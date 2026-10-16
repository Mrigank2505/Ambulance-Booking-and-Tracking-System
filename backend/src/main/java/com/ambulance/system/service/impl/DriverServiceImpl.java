package com.ambulance.system.service.impl;

import com.ambulance.system.dto.BookingDTO;
import com.ambulance.system.dto.DriverAvailabilityRequest;
import com.ambulance.system.dto.DriverDTO;
import com.ambulance.system.dto.LocationUpdateRequest;
import com.ambulance.system.entity.*;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.exception.UnauthorizedException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.AmbulanceLocationRepository;
import com.ambulance.system.repository.AmbulanceRepository;
import com.ambulance.system.repository.BookingRepository;
import com.ambulance.system.repository.DriverRepository;
import com.ambulance.system.repository.UserRepository;
import com.ambulance.system.security.CustomUserDetails;
import com.ambulance.system.service.DriverService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final BookingRepository bookingRepository;
    private final AmbulanceLocationRepository locationRepository;
    private final EntityDtoMapper mapper;

    public DriverServiceImpl(DriverRepository driverRepository,
                             UserRepository userRepository,
                             AmbulanceRepository ambulanceRepository,
                             BookingRepository bookingRepository,
                             AmbulanceLocationRepository locationRepository,
                             EntityDtoMapper mapper) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.bookingRepository = bookingRepository;
        this.locationRepository = locationRepository;
        this.mapper = mapper;
    }

    private Driver getAuthenticatedDriver() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new UnauthorizedException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return driverRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No driver profile associated with this account"));
    }

    @Override
    public List<DriverDTO> getAllDrivers() {
        return driverRepository.findAll().stream().map(driver -> {
            DriverDTO dto = mapper.toDriverDTO(driver);
            ambulanceRepository.findByDriverId(driver.getId())
                    .ifPresent(amb -> dto.setAssignedAmbulanceNumber(amb.getAmbulanceNumber()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public DriverDTO getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        DriverDTO dto = mapper.toDriverDTO(driver);
        ambulanceRepository.findByDriverId(driver.getId())
                .ifPresent(amb -> dto.setAssignedAmbulanceNumber(amb.getAmbulanceNumber()));
        return dto;
    }

    @Override
    public DriverDTO getCurrentDriver() {
        Driver driver = getAuthenticatedDriver();
        DriverDTO dto = mapper.toDriverDTO(driver);
        ambulanceRepository.findByDriverId(driver.getId())
                .ifPresent(amb -> dto.setAssignedAmbulanceNumber(amb.getAmbulanceNumber()));
        return dto;
    }

    @Override
    @Transactional
    public DriverDTO updateAvailability(Long driverId, DriverAvailabilityRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));
        driver.setAvailabilityStatus(request.getAvailabilityStatus());
        Driver updated = driverRepository.save(driver);

        // Also update ambulance status if linked
        ambulanceRepository.findByDriverId(driver.getId()).ifPresent(amb -> {
            if (request.getAvailabilityStatus() == DriverAvailability.OFFLINE) {
                amb.setAvailabilityStatus(AmbulanceAvailability.OFFLINE);
            } else if (request.getAvailabilityStatus() == DriverAvailability.ONLINE && amb.getAvailabilityStatus() == AmbulanceAvailability.OFFLINE) {
                amb.setAvailabilityStatus(AmbulanceAvailability.AVAILABLE);
            }
            ambulanceRepository.save(amb);
        });

        return mapper.toDriverDTO(updated);
    }

    @Override
    @Transactional
    public DriverDTO updateCurrentDriverAvailability(DriverAvailabilityRequest request) {
        Driver driver = getAuthenticatedDriver();
        return updateAvailability(driver.getId(), request);
    }

    @Override
    @Transactional
    public DriverDTO updateLocation(Long driverId, LocationUpdateRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        Driver updated = driverRepository.save(driver);

        ambulanceRepository.findByDriverId(driver.getId()).ifPresent(amb -> {
            amb.setCurrentLatitude(request.getLatitude());
            amb.setCurrentLongitude(request.getLongitude());
            ambulanceRepository.save(amb);

            // Record location history
            AmbulanceLocation loc = new AmbulanceLocation();
            loc.setAmbulance(amb);
            loc.setLatitude(request.getLatitude());
            loc.setLongitude(request.getLongitude());
            loc.setSpeed(request.getSpeed() != null ? request.getSpeed() : 35.0);
            loc.setTimestamp(LocalDateTime.now());
            locationRepository.save(loc);
        });

        return mapper.toDriverDTO(updated);
    }

    @Override
    @Transactional
    public DriverDTO updateCurrentDriverLocation(LocationUpdateRequest request) {
        Driver driver = getAuthenticatedDriver();
        return updateLocation(driver.getId(), request);
    }

    @Override
    public List<BookingDTO> getPendingBookingsForCurrentDriver() {
        Driver driver = getAuthenticatedDriver();
        List<BookingStatus> pending = Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.SEARCHING, BookingStatus.REQUESTED);
        return bookingRepository.findByDriverIdAndBookingStatusInOrderByCreatedAtDesc(driver.getId(), pending)
                .stream()
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getActiveBookingsForCurrentDriver() {
        Driver driver = getAuthenticatedDriver();
        List<BookingStatus> active = Arrays.asList(
                BookingStatus.DRIVER_ACCEPTED,
                BookingStatus.ON_THE_WAY,
                BookingStatus.ARRIVED,
                BookingStatus.PATIENT_PICKED_UP,
                BookingStatus.REACHED_HOSPITAL
        );
        return bookingRepository.findByDriverIdAndBookingStatusInOrderByCreatedAtDesc(driver.getId(), active)
                .stream()
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());
    }
}
