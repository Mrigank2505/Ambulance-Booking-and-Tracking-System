package com.ambulance.system.service.impl;

import com.ambulance.system.dto.AdminDashboardDTO;
import com.ambulance.system.dto.BookingDTO;
import com.ambulance.system.dto.UserDTO;
import com.ambulance.system.entity.*;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.*;
import com.ambulance.system.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final BookingRepository bookingRepository;
    private final EntityDtoMapper mapper;

    public AdminServiceImpl(UserRepository userRepository,
                            DriverRepository driverRepository,
                            AmbulanceRepository ambulanceRepository,
                            BookingRepository bookingRepository,
                            EntityDtoMapper mapper) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.bookingRepository = bookingRepository;
        this.mapper = mapper;
    }

    @Override
    public AdminDashboardDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalDrivers = driverRepository.count();
        long totalAmbulances = ambulanceRepository.count();
        long availableAmbulances = ambulanceRepository.countByAvailabilityStatus(AmbulanceAvailability.AVAILABLE);
        long busyAmbulances = totalAmbulances - availableAmbulances;

        long completedBookings = bookingRepository.countByBookingStatus(BookingStatus.COMPLETED);
        long cancelledBookings = bookingRepository.countByBookingStatus(BookingStatus.CANCELLED);
        long totalBookings = bookingRepository.count();
        long activeBookings = Math.max(0, totalBookings - (completedBookings + cancelledBookings));

        long emergencyBookings = bookingRepository.countByEmergencyType(EmergencyType.CRITICAL)
                + bookingRepository.countByEmergencyType(EmergencyType.SERIOUS);

        long onlineDrivers = driverRepository.countByAvailabilityStatus(DriverAvailability.ONLINE);

        List<BookingDTO> recentBookings = bookingRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .limit(10)
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());

        return new AdminDashboardDTO(
                totalUsers,
                totalDrivers,
                totalAmbulances,
                availableAmbulances,
                busyAmbulances,
                activeBookings,
                completedBookings,
                cancelledBookings,
                emergencyBookings,
                onlineDrivers,
                recentBookings
        );
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setStatus(status);
        return mapper.toUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setRole(role);
        return mapper.toUserDTO(userRepository.save(user));
    }
}
