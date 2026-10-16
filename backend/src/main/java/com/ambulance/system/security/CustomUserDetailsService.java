package com.ambulance.system.security;

import com.ambulance.system.entity.Driver;
import com.ambulance.system.entity.User;
import com.ambulance.system.repository.DriverRepository;
import com.ambulance.system.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    public CustomUserDetailsService(UserRepository userRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Identifier cannot be blank");
        }
        String clean = identifier.trim();

        // 1. Match by exact or case-insensitive email
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(clean);

        // 2. Match by phone number
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhone(clean);
        }

        // 3. Match by driver license number (e.g. DL-2024-8841)
        if (userOpt.isEmpty()) {
            userOpt = driverRepository.findByLicenseNumberIgnoreCase(clean).map(Driver::getUser);
        }

        // 4. Match by email prefix/username (e.g., "admin" -> "admin@ambulance.com", "driver.mike" -> "driver.mike@ambulance.com", "john" -> "john@example.com")
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findFirstByEmailStartingWithIgnoreCase(clean.toLowerCase() + "@");
        }

        // 5. Match by full name (e.g., "System Administrator", "Mike Ross", "John Doe")
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByFullNameIgnoreCase(clean);
        }

        // 6. Match by numeric ID (User ID or Driver ID)
        if (userOpt.isEmpty() && clean.matches("^\\d+$")) {
            try {
                Long id = Long.parseLong(clean);
                userOpt = userRepository.findById(id);
                if (userOpt.isEmpty()) {
                    userOpt = driverRepository.findById(id).map(Driver::getUser);
                }
            } catch (NumberFormatException ignored) {}
        }

        User user = userOpt.orElseThrow(() -> 
                new UsernameNotFoundException("User not found with identifier: " + identifier));

        return new CustomUserDetails(user);
    }
}
