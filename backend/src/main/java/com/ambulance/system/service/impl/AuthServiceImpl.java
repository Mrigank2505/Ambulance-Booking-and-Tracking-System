package com.ambulance.system.service.impl;

import com.ambulance.system.dto.LoginRequest;
import com.ambulance.system.dto.LoginResponse;
import com.ambulance.system.dto.RegisterRequest;
import com.ambulance.system.dto.UserDTO;
import com.ambulance.system.entity.Driver;
import com.ambulance.system.entity.DriverAvailability;
import com.ambulance.system.entity.Role;
import com.ambulance.system.entity.User;
import com.ambulance.system.entity.UserStatus;
import com.ambulance.system.exception.BadRequestException;
import com.ambulance.system.exception.UnauthorizedException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.DriverRepository;
import com.ambulance.system.repository.UserRepository;
import com.ambulance.system.security.CustomUserDetails;
import com.ambulance.system.security.JwtTokenProvider;
import com.ambulance.system.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EntityDtoMapper mapper;

    @Value("${app.security.admin-key:ADMIN_SECURE_KEY_2026}")
    private String adminSecurityKey;

    public AuthServiceImpl(UserRepository userRepository, DriverRepository driverRepository,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider, EntityDtoMapper mapper) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.mapper = mapper;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.getEmail());

        String identifier = request.getEmail() != null ? request.getEmail().trim() : "";
        Optional<User> lookupOpt = userRepository.findByEmailIgnoreCase(identifier);
        if (lookupOpt.isEmpty()) {
            lookupOpt = userRepository.findByPhone(identifier);
        }
        if (lookupOpt.isEmpty()) {
            lookupOpt = driverRepository.findByLicenseNumberIgnoreCase(identifier).map(Driver::getUser);
        }
        if (lookupOpt.isEmpty()) {
            lookupOpt = userRepository.findFirstByEmailStartingWithIgnoreCase(identifier.toLowerCase() + "@");
        }
        if (lookupOpt.isEmpty()) {
            lookupOpt = userRepository.findByFullNameIgnoreCase(identifier);
        }

        if (lookupOpt.isPresent()) {
            User target = lookupOpt.get();
            if (target.getStatus() == UserStatus.PENDING_APPROVAL) {
                throw new BadRequestException("Your account is pending administrator approval. Please wait for authorization before logging in.");
            }
            if (target.getStatus() == UserStatus.SUSPENDED) {
                throw new BadRequestException("Your account has been suspended. Please contact administrator.");
            }
            if (target.getStatus() == UserStatus.INACTIVE) {
                throw new BadRequestException("Your account is currently inactive. Please contact support.");
            }
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BadRequestException("User profile not found"));

        log.info("Login successful for user: {} ({})", user.getEmail(), user.getRole());
        return new LoginResponse(jwt, mapper.toUserDTO(user));
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("Registration request for email: {} with role: {}", request.getEmail(), request.getRole());

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        if (request.getConfirmPassword() != null && !request.getConfirmPassword().isBlank()) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new BadRequestException("Password and confirm password do not match");
            }
        }

        String cleanEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (cleanEmail.isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("An account with this email address already exists");
        }

        String cleanPhone = request.getPhone() != null ? request.getPhone().trim() : "";
        if (cleanPhone.isEmpty()) {
            throw new BadRequestException("Phone number is required");
        }
        if (userRepository.existsByPhone(cleanPhone)) {
            throw new BadRequestException("An account with this phone number already exists");
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.USER;

        // Security check for ADMIN registration: require authorized administrator security key
        if (assignedRole == Role.ADMIN) {
            String key = request.getAdminSecurityKey() != null ? request.getAdminSecurityKey().trim() : "";
            if (key.isEmpty() || !key.equals(adminSecurityKey)) {
                log.warn("Unauthorized attempt to self-register as ADMIN for email: {}", cleanEmail);
                throw new BadRequestException("Administrator registration requires a valid administrator invitation key or authorization code. Public self-registration for administrator privileges is restricted.");
            }
        }

        // Validation for DRIVER registration: require driver licence number and ensure uniqueness
        if (assignedRole == Role.DRIVER) {
            if (request.getLicenseNumber() == null || request.getLicenseNumber().trim().isEmpty()) {
                throw new BadRequestException("Driver licence number is required for driver registration");
            }
            String cleanLicense = request.getLicenseNumber().trim().toUpperCase();
            if (driverRepository.existsByLicenseNumberIgnoreCase(cleanLicense)) {
                throw new BadRequestException("A driver profile with licence number '" + cleanLicense + "' already exists");
            }
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(cleanEmail);
        user.setPhone(cleanPhone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setRole(assignedRole);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // If registered as driver, create and associate corresponding driver profile record
        if (assignedRole == Role.DRIVER) {
            Driver driver = new Driver();
            driver.setUser(savedUser);
            driver.setLicenseNumber(request.getLicenseNumber().trim().toUpperCase());
            driver.setAvailabilityStatus(DriverAvailability.OFFLINE);
            driver.setCurrentLatitude(12.9716); // default city center
            driver.setCurrentLongitude(77.5946);
            driverRepository.save(driver);
            log.info("Driver profile record created and linked for user {} with license {}", savedUser.getEmail(), driver.getLicenseNumber());
        }

        log.info("Successfully registered user: {} with role: {}", savedUser.getEmail(), savedUser.getRole());
        return mapper.toUserDTO(savedUser);
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new UnauthorizedException("User is not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        return mapper.toUserDTO(user);
    }
}
