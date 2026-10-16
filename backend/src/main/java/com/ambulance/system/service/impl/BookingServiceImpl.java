package com.ambulance.system.service.impl;

import com.ambulance.system.dto.BookingDTO;
import com.ambulance.system.dto.BookingRequest;
import com.ambulance.system.dto.BookingStatusUpdateRequest;
import com.ambulance.system.entity.*;
import com.ambulance.system.exception.BadRequestException;
import com.ambulance.system.exception.InvalidOperationException;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.exception.UnauthorizedException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.*;
import com.ambulance.system.security.CustomUserDetails;
import com.ambulance.system.service.AmbulanceService;
import com.ambulance.system.service.BookingService;
import com.ambulance.system.service.NotificationService;
import com.ambulance.system.util.DistanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final DriverRepository driverRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceService ambulanceService;
    private final NotificationService notificationService;
    private final DistanceUtil distanceUtil;
    private final EntityDtoMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              AmbulanceRepository ambulanceRepository,
                              DriverRepository driverRepository,
                              HospitalRepository hospitalRepository,
                              AmbulanceService ambulanceService,
                              NotificationService notificationService,
                              DistanceUtil distanceUtil,
                              EntityDtoMapper mapper,
                              SimpMessagingTemplate messagingTemplate) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.driverRepository = driverRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceService = ambulanceService;
        this.notificationService = notificationService;
        this.distanceUtil = distanceUtil;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new UnauthorizedException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private Driver getAuthenticatedDriver() {
        User user = getAuthenticatedUser();
        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user is not registered as an active driver"));
    }

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequest request) {
        User user = getAuthenticatedUser();
        log.info("Received booking request from user: {} for patient: {}", user.getEmail(), request.getPatientName());

        // 1. Generate unique booking code: AMB-BK-YYYYMMDD-XXXX
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomSuffix = 1000 + new Random().nextInt(9000);
        String bookingCode = "AMB-BK-" + datePrefix + "-" + randomSuffix;

        // 2. Identify destination hospital if specified
        Hospital destinationHospital = null;
        if (request.getHospitalId() != null) {
            destinationHospital = hospitalRepository.findById(request.getHospitalId()).orElse(null);
        } else if (request.getDestinationLatitude() != null && request.getDestinationLongitude() != null) {
            // Find nearest emergency hospital to destination coords
            List<Hospital> hospitals = hospitalRepository.findAll();
            destinationHospital = hospitals.stream()
                    .min((h1, h2) -> Double.compare(
                            distanceUtil.calculateDistance(request.getDestinationLatitude(), request.getDestinationLongitude(), h1.getLatitude(), h1.getLongitude()),
                            distanceUtil.calculateDistance(request.getDestinationLatitude(), request.getDestinationLongitude(), h2.getLatitude(), h2.getLongitude())
                    )).orElse(null);
        }

        // 3. Find and allocate nearest available ambulance
        Ambulance assignedAmbulance = ambulanceService.findNearestAvailableAmbulance(
                request.getPickupLatitude(), request.getPickupLongitude(), request.getAmbulanceType()
        );

        Driver assignedDriver = null;
        double distanceKm = 0.0;
        String etaStr = "Calculating...";
        BookingStatus initialStatus = BookingStatus.REQUESTED;

        if (assignedAmbulance != null) {
            distanceKm = distanceUtil.calculateDistance(
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    assignedAmbulance.getCurrentLatitude(), assignedAmbulance.getCurrentLongitude()
            );
            int etaMin = distanceUtil.estimateEtaMinutes(distanceKm);
            etaStr = distanceUtil.formatEta(etaMin);

            assignedDriver = assignedAmbulance.getDriver();
            assignedAmbulance.setAvailabilityStatus(AmbulanceAvailability.ASSIGNED);
            ambulanceRepository.save(assignedAmbulance);

            if (assignedDriver != null) {
                assignedDriver.setAvailabilityStatus(DriverAvailability.BUSY);
                driverRepository.save(assignedDriver);
            }
            initialStatus = BookingStatus.ASSIGNED;
        } else {
            initialStatus = BookingStatus.SEARCHING;
            log.warn("No immediate ambulance available. Setting status to SEARCHING");
        }

        // 4. Construct Booking entity
        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setUser(user);
        booking.setAmbulance(assignedAmbulance);
        booking.setDriver(assignedDriver);
        booking.setHospital(destinationHospital);
        booking.setPatientName(request.getPatientName().trim());
        booking.setPatientPhone(request.getPatientPhone().trim());
        booking.setPickupAddress(request.getPickupAddress().trim());
        booking.setPickupLatitude(request.getPickupLatitude());
        booking.setPickupLongitude(request.getPickupLongitude());
        booking.setDestinationAddress(request.getDestinationAddress() != null ? request.getDestinationAddress().trim() : (destinationHospital != null ? destinationHospital.getName() : "General Hospital"));
        booking.setDestinationLatitude(request.getDestinationLatitude() != null ? request.getDestinationLatitude() : (destinationHospital != null ? destinationHospital.getLatitude() : null));
        booking.setDestinationLongitude(request.getDestinationLongitude() != null ? request.getDestinationLongitude() : (destinationHospital != null ? destinationHospital.getLongitude() : null));
        booking.setEmergencyType(request.getEmergencyType());
        booking.setAmbulanceType(request.getAmbulanceType());
        booking.setBookingStatus(initialStatus);
        booking.setNotes(request.getNotes());
        booking.setEstimatedArrivalTime(etaStr);
        booking.setDistanceKm(distanceKm);

        Booking savedBooking = bookingRepository.save(booking);

        // 5. Send notifications
        notificationService.sendNotification(
                user, savedBooking, "Booking Confirmed (" + bookingCode + ")",
                assignedAmbulance != null
                        ? "Ambulance " + assignedAmbulance.getAmbulanceNumber() + " assigned. ETA: " + etaStr
                        : "Your emergency booking request is currently being prioritized.",
                NotificationType.BOOKING
        );

        if (assignedDriver != null && assignedDriver.getUser() != null) {
            notificationService.sendNotification(
                    assignedDriver.getUser(), savedBooking, "Urgent Dispatch Alert",
                    "New emergency assigned: " + booking.getEmergencyType() + " at " + booking.getPickupAddress(),
                    NotificationType.ALERT
            );
        }

        // 6. Broadcast through WebSocket
        broadcastBookingUpdate(savedBooking);

        return mapper.toBookingDTO(savedBooking);
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            boolean isOwner = booking.getUser() != null && booking.getUser().getId().equals(userDetails.getId());
            boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isDriver = booking.getDriver() != null && booking.getDriver().getUser() != null
                    && booking.getDriver().getUser().getId().equals(userDetails.getId());

            if (!isOwner && !isAdmin && !isDriver) {
                throw new UnauthorizedException("You do not have permission to view this booking");
            }
        }

        return mapper.toBookingDTO(booking);
    }

    @Override
    public BookingDTO getBookingByCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with code: " + bookingCode));
        return mapper.toBookingDTO(booking);
    }

    @Override
    public List<BookingDTO> getCurrentUserBookings() {
        User user = getAuthenticatedUser();
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .map(mapper::toBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDTO cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        User currentUser = getAuthenticatedUser();
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to cancel this booking");
        }

        // Validate state: only cancellable before patient pickup
        if (booking.getBookingStatus() == BookingStatus.COMPLETED
                || booking.getBookingStatus() == BookingStatus.CANCELLED
                || booking.getBookingStatus() == BookingStatus.PATIENT_PICKED_UP
                || booking.getBookingStatus() == BookingStatus.REACHED_HOSPITAL) {
            throw new InvalidOperationException("Cannot cancel booking when trip is in state: " + booking.getBookingStatus());
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            booking.setNotes((booking.getNotes() != null ? booking.getNotes() + "\n" : "") + "Cancellation reason: " + reason);
        }

        // Release ambulance and driver
        releaseAmbulanceAndDriver(booking);

        Booking saved = bookingRepository.save(booking);

        notificationService.sendNotification(
                booking.getUser(), saved, "Booking Cancelled",
                "Your booking " + booking.getBookingCode() + " has been cancelled.",
                NotificationType.STATUS
        );

        broadcastBookingUpdate(saved);
        return mapper.toBookingDTO(saved);
    }

    @Override
    @Transactional
    public BookingDTO driverAcceptBooking(Long id) {
        Driver driver = getAuthenticatedDriver();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getDriver() == null || !booking.getDriver().getId().equals(driver.getId())) {
            throw new UnauthorizedException("This booking is not assigned to you");
        }

        if (booking.getBookingStatus() != BookingStatus.ASSIGNED && booking.getBookingStatus() != BookingStatus.REQUESTED) {
            throw new InvalidOperationException("Cannot accept booking in status: " + booking.getBookingStatus());
        }

        booking.setBookingStatus(BookingStatus.DRIVER_ACCEPTED);
        if (booking.getAmbulance() != null) {
            booking.getAmbulance().setAvailabilityStatus(AmbulanceAvailability.ON_TRIP);
            ambulanceRepository.save(booking.getAmbulance());
        }
        Booking saved = bookingRepository.save(booking);

        notificationService.sendNotification(
                booking.getUser(), saved, "Ambulance Driver Confirmed",
                "Driver " + driver.getUser().getFullName() + " accepted your trip and is preparing for dispatch.",
                NotificationType.STATUS
        );

        broadcastBookingUpdate(saved);
        return mapper.toBookingDTO(saved);
    }

    @Override
    @Transactional
    public BookingDTO driverRejectBooking(Long id, String reason) {
        Driver driver = getAuthenticatedDriver();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getDriver() == null || !booking.getDriver().getId().equals(driver.getId())) {
            throw new UnauthorizedException("This booking is not assigned to you");
        }

        // Release current driver
        driver.setAvailabilityStatus(DriverAvailability.ONLINE);
        driverRepository.save(driver);

        if (booking.getAmbulance() != null) {
            booking.getAmbulance().setAvailabilityStatus(AmbulanceAvailability.AVAILABLE);
            ambulanceRepository.save(booking.getAmbulance());
        }

        // Reassign to another nearest ambulance
        Ambulance alternative = ambulanceService.findNearestAvailableAmbulance(
                booking.getPickupLatitude(), booking.getPickupLongitude(), booking.getAmbulanceType()
        );

        if (alternative != null && !alternative.getId().equals(booking.getAmbulance().getId())) {
            booking.setAmbulance(alternative);
            booking.setDriver(alternative.getDriver());
            booking.setBookingStatus(BookingStatus.ASSIGNED);
            alternative.setAvailabilityStatus(AmbulanceAvailability.ASSIGNED);
            ambulanceRepository.save(alternative);
        } else {
            booking.setAmbulance(null);
            booking.setDriver(null);
            booking.setBookingStatus(BookingStatus.SEARCHING);
        }

        Booking saved = bookingRepository.save(booking);
        broadcastBookingUpdate(saved);
        return mapper.toBookingDTO(saved);
    }

    @Override
    @Transactional
    public BookingDTO updateBookingStatus(Long id, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        validateStatusTransition(booking.getBookingStatus(), request.getBookingStatus());

        booking.setBookingStatus(request.getBookingStatus());
        if (request.getNotes() != null) {
            booking.setNotes((booking.getNotes() != null ? booking.getNotes() + "\n" : "") + request.getNotes());
        }

        if (request.getBookingStatus() == BookingStatus.COMPLETED) {
            releaseAmbulanceAndDriver(booking);
        }

        Booking saved = bookingRepository.save(booking);

        notificationService.sendNotification(
                booking.getUser(), saved, "Status Update: " + request.getBookingStatus(),
                "Your booking " + booking.getBookingCode() + " status changed to " + request.getBookingStatus(),
                NotificationType.STATUS
        );

        broadcastBookingUpdate(saved);
        return mapper.toBookingDTO(saved);
    }

    @Override
    @Transactional
    public BookingDTO driverMarkArrived(Long id) {
        return updateBookingStatus(id, new BookingStatusUpdateRequest(BookingStatus.ARRIVED, "Ambulance arrived at patient location."));
    }

    @Override
    @Transactional
    public BookingDTO driverMarkPickedUp(Long id) {
        return updateBookingStatus(id, new BookingStatusUpdateRequest(BookingStatus.PATIENT_PICKED_UP, "Patient picked up and en-route to hospital."));
    }

    @Override
    @Transactional
    public BookingDTO driverMarkCompleted(Long id) {
        return updateBookingStatus(id, new BookingStatusUpdateRequest(BookingStatus.COMPLETED, "Patient delivered safely to destination hospital."));
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus target) {
        if (current == target) return;

        boolean valid = switch (current) {
            case REQUESTED -> target == BookingStatus.SEARCHING || target == BookingStatus.ASSIGNED || target == BookingStatus.CANCELLED;
            case SEARCHING -> target == BookingStatus.ASSIGNED || target == BookingStatus.CANCELLED;
            case ASSIGNED -> target == BookingStatus.DRIVER_ACCEPTED || target == BookingStatus.REJECTED || target == BookingStatus.CANCELLED;
            case DRIVER_ACCEPTED -> target == BookingStatus.ON_THE_WAY || target == BookingStatus.ARRIVED || target == BookingStatus.CANCELLED;
            case ON_THE_WAY -> target == BookingStatus.ARRIVED || target == BookingStatus.CANCELLED;
            case ARRIVED -> target == BookingStatus.PATIENT_PICKED_UP || target == BookingStatus.CANCELLED;
            case PATIENT_PICKED_UP -> target == BookingStatus.REACHED_HOSPITAL || target == BookingStatus.COMPLETED;
            case REACHED_HOSPITAL -> target == BookingStatus.COMPLETED;
            case COMPLETED, CANCELLED, REJECTED -> false;
        };

        if (!valid) {
            throw new InvalidOperationException("Illegal booking status transition from " + current + " to " + target);
        }
    }

    private void releaseAmbulanceAndDriver(Booking booking) {
        if (booking.getAmbulance() != null) {
            booking.getAmbulance().setAvailabilityStatus(AmbulanceAvailability.AVAILABLE);
            ambulanceRepository.save(booking.getAmbulance());
        }
        if (booking.getDriver() != null) {
            booking.getDriver().setAvailabilityStatus(DriverAvailability.ONLINE);
            driverRepository.save(booking.getDriver());
        }
    }

    private void broadcastBookingUpdate(Booking booking) {
        try {
            BookingDTO dto = mapper.toBookingDTO(booking);
            messagingTemplate.convertAndSend("/topic/booking/" + booking.getId(), dto);
        } catch (Exception e) {
            log.warn("Failed to broadcast booking update via WebSocket: {}", e.getMessage());
        }
    }
}
