package com.ambulance.system.service.impl;

import com.ambulance.system.dto.LocationPingRequest;
import com.ambulance.system.dto.TrackingDTO;
import com.ambulance.system.entity.Ambulance;
import com.ambulance.system.entity.AmbulanceLocation;
import com.ambulance.system.entity.Booking;
import com.ambulance.system.entity.BookingStatus;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.repository.AmbulanceLocationRepository;
import com.ambulance.system.repository.AmbulanceRepository;
import com.ambulance.system.repository.BookingRepository;
import com.ambulance.system.service.TrackingService;
import com.ambulance.system.util.DistanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackingServiceImpl implements TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingServiceImpl.class);

    private final AmbulanceRepository ambulanceRepository;
    private final BookingRepository bookingRepository;
    private final AmbulanceLocationRepository locationRepository;
    private final DistanceUtil distanceUtil;
    private final SimpMessagingTemplate messagingTemplate;

    public TrackingServiceImpl(AmbulanceRepository ambulanceRepository,
                               BookingRepository bookingRepository,
                               AmbulanceLocationRepository locationRepository,
                               DistanceUtil distanceUtil,
                               SimpMessagingTemplate messagingTemplate) {
        this.ambulanceRepository = ambulanceRepository;
        this.bookingRepository = bookingRepository;
        this.locationRepository = locationRepository;
        this.distanceUtil = distanceUtil;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public TrackingDTO recordLocation(LocationPingRequest request) {
        Ambulance ambulance = ambulanceRepository.findById(request.getAmbulanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with id: " + request.getAmbulanceId()));

        ambulance.setCurrentLatitude(request.getLatitude());
        ambulance.setCurrentLongitude(request.getLongitude());
        ambulanceRepository.save(ambulance);

        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId()).orElse(null);
        }

        AmbulanceLocation loc = new AmbulanceLocation();
        loc.setAmbulance(ambulance);
        loc.setBooking(booking);
        loc.setLatitude(request.getLatitude());
        loc.setLongitude(request.getLongitude());
        loc.setSpeed(request.getSpeed() != null ? request.getSpeed() : 35.0);
        loc.setTimestamp(LocalDateTime.now());
        locationRepository.save(loc);

        TrackingDTO trackingDTO = buildTrackingDTO(ambulance, booking, request.getLatitude(), request.getLongitude(), loc.getSpeed());

        // Broadcast to WebSocket topics
        try {
            messagingTemplate.convertAndSend("/topic/ambulance/" + ambulance.getId(), trackingDTO);
            if (booking != null) {
                messagingTemplate.convertAndSend("/topic/booking/" + booking.getId(), trackingDTO);
            }
        } catch (Exception ex) {
            log.warn("Could not broadcast tracking update through WebSocket: {}", ex.getMessage());
        }

        return trackingDTO;
    }

    @Override
    public TrackingDTO getLatestTrackingForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getAmbulance() == null) {
            throw new ResourceNotFoundException("No ambulance assigned yet for booking: " + booking.getBookingCode());
        }

        Ambulance ambulance = booking.getAmbulance();
        return buildTrackingDTO(ambulance, booking, ambulance.getCurrentLatitude(), ambulance.getCurrentLongitude(), 35.0);
    }

    @Override
    public TrackingDTO getLatestTrackingForAmbulance(Long ambulanceId) {
        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with id: " + ambulanceId));
        return buildTrackingDTO(ambulance, null, ambulance.getCurrentLatitude(), ambulance.getCurrentLongitude(), 0.0);
    }

    @Override
    public List<TrackingDTO> getLocationHistoryForBooking(Long bookingId) {
        return locationRepository.findByBookingIdOrderByTimestampAsc(bookingId).stream().map(loc -> {
            TrackingDTO dto = new TrackingDTO();
            dto.setAmbulanceId(loc.getAmbulance().getId());
            dto.setAmbulanceNumber(loc.getAmbulance().getAmbulanceNumber());
            dto.setBookingId(bookingId);
            dto.setLatitude(loc.getLatitude());
            dto.setLongitude(loc.getLongitude());
            dto.setSpeed(loc.getSpeed());
            dto.setTimestamp(loc.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TrackingDTO simulateDemoStep(Long bookingId, double stepRatio) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        Ambulance ambulance = booking.getAmbulance();
        if (ambulance == null) {
            throw new ResourceNotFoundException("Ambulance not yet assigned for demo simulation");
        }

        // Clamp step ratio between 0.0 and 1.0
        double ratio = Math.max(0.0, Math.min(1.0, stepRatio));

        // Start from ambulance original dispatch station coords or hospital, moving to pickup coords
        double targetLat = booking.getPickupLatitude();
        double targetLon = booking.getPickupLongitude();

        // If trip is reached/picked up, target is destination hospital coords
        if (booking.getBookingStatus() == BookingStatus.PATIENT_PICKED_UP || booking.getBookingStatus() == BookingStatus.REACHED_HOSPITAL) {
            if (booking.getDestinationLatitude() != null && booking.getDestinationLongitude() != null) {
                targetLat = booking.getDestinationLatitude();
                targetLon = booking.getDestinationLongitude();
            }
        }

        // Interpolate position
        double currentAmbLat = ambulance.getCurrentLatitude();
        double currentAmbLon = ambulance.getCurrentLongitude();

        double nextLat = currentAmbLat + (targetLat - currentAmbLat) * ratio;
        double nextLon = currentAmbLon + (targetLon - currentAmbLon) * ratio;

        LocationPingRequest ping = new LocationPingRequest(ambulance.getId(), booking.getId(), nextLat, nextLon, 45.0);
        return recordLocation(ping);
    }

    private TrackingDTO buildTrackingDTO(Ambulance ambulance, Booking booking, double lat, double lon, double speed) {
        TrackingDTO dto = new TrackingDTO();
        dto.setAmbulanceId(ambulance.getId());
        dto.setAmbulanceNumber(ambulance.getAmbulanceNumber());
        dto.setLatitude(lat);
        dto.setLongitude(lon);
        dto.setSpeed(speed);
        dto.setTimestamp(LocalDateTime.now());

        if (ambulance.getDriver() != null && ambulance.getDriver().getUser() != null) {
            dto.setDriverName(ambulance.getDriver().getUser().getFullName());
            dto.setDriverPhone(ambulance.getDriver().getUser().getPhone());
        }

        if (booking != null) {
            dto.setBookingId(booking.getId());
            dto.setBookingCode(booking.getBookingCode());
            dto.setStatus(booking.getBookingStatus());

            // Target destination coordinates
            double destLat = booking.getPickupLatitude();
            double destLon = booking.getPickupLongitude();

            if (booking.getBookingStatus() == BookingStatus.PATIENT_PICKED_UP || booking.getBookingStatus() == BookingStatus.REACHED_HOSPITAL) {
                if (booking.getDestinationLatitude() != null && booking.getDestinationLongitude() != null) {
                    destLat = booking.getDestinationLatitude();
                    destLon = booking.getDestinationLongitude();
                }
            }

            double remainingDist = distanceUtil.calculateDistance(lat, lon, destLat, destLon);
            int etaMin = distanceUtil.estimateEtaMinutes(remainingDist);

            dto.setDistanceRemainingKm(remainingDist);
            dto.setEta(distanceUtil.formatEta(etaMin));
        }

        return dto;
    }
}
