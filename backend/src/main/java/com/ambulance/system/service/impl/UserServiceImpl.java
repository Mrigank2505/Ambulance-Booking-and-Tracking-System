package com.ambulance.system.service.impl;

import com.ambulance.system.dto.EmergencyContactDTO;
import com.ambulance.system.dto.PasswordChangeRequest;
import com.ambulance.system.dto.UserDTO;
import com.ambulance.system.dto.UserProfileUpdateRequest;
import com.ambulance.system.entity.EmergencyContact;
import com.ambulance.system.entity.User;
import com.ambulance.system.exception.BadRequestException;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.exception.UnauthorizedException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.EmergencyContactRepository;
import com.ambulance.system.repository.UserRepository;
import com.ambulance.system.security.CustomUserDetails;
import com.ambulance.system.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityDtoMapper mapper;

    public UserServiceImpl(UserRepository userRepository,
                           EmergencyContactRepository emergencyContactRepository,
                           PasswordEncoder passwordEncoder,
                           EntityDtoMapper mapper) {
        this.userRepository = userRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new UnauthorizedException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("User account not found"));
    }

    @Override
    public UserDTO getCurrentUserProfile() {
        return mapper.toUserDTO(getAuthenticatedUser());
    }

    @Override
    @Transactional
    public UserDTO updateProfile(UserProfileUpdateRequest request) {
        User user = getAuthenticatedUser();
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());
        user.setAddress(request.getAddress());
        User updated = userRepository.save(user);
        return mapper.toUserDTO(updated);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getAuthenticatedUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<EmergencyContactDTO> getEmergencyContacts() {
        User user = getAuthenticatedUser();
        return emergencyContactRepository.findByUserId(user.getId())
                .stream()
                .map(mapper::toEmergencyContactDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmergencyContactDTO addEmergencyContact(EmergencyContactDTO request) {
        User user = getAuthenticatedUser();
        EmergencyContact contact = new EmergencyContact();
        contact.setUser(user);
        contact.setName(request.getName().trim());
        contact.setPhone(request.getPhone().trim());
        contact.setRelationship(request.getRelationship());
        EmergencyContact saved = emergencyContactRepository.save(contact);
        return mapper.toEmergencyContactDTO(saved);
    }

    @Override
    @Transactional
    public void deleteEmergencyContact(Long contactId) {
        User user = getAuthenticatedUser();
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found"));
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this contact");
        }
        emergencyContactRepository.delete(contact);
    }
}
