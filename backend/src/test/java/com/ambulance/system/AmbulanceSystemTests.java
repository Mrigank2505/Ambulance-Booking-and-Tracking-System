package com.ambulance.system;

import com.ambulance.system.dto.*;
import com.ambulance.system.entity.*;
import com.ambulance.system.repository.*;
import com.ambulance.system.service.*;
import com.ambulance.system.util.DistanceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class AmbulanceSystemTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private AmbulanceService ambulanceService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private TrackingService trackingService;

    @Autowired
    private DistanceUtil distanceUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private Driver testDriver;

    @BeforeEach
    void setupContext() {
        testUser = userRepository.findByEmail("john@example.com").orElse(null);
        if (testUser != null) {
            com.ambulance.system.security.CustomUserDetails userDetails = new com.ambulance.system.security.CustomUserDetails(testUser);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    @Test
    void testDistanceCalculationHaversine() {
        // Distance between two points in Bangalore (roughly ~1.4 km)
        double dist = distanceUtil.calculateDistance(12.9716, 77.5946, 12.9610, 77.6010);
        assertTrue(dist > 1.0 && dist < 2.0, "Calculated distance should be approximately 1.4 km, got: " + dist);

        int eta = distanceUtil.estimateEtaMinutes(dist);
        assertTrue(eta >= 1 && eta <= 10, "ETA should be realistic for short trip");
    }

    @Test
    void testAuthLoginSuccess() {
        LoginResponse response = authService.login(new LoginRequest("admin@ambulance.com", "Password@123"));
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin@ambulance.com", response.getUser().getEmail());
        assertEquals(Role.ADMIN, response.getUser().getRole());
    }

    @Test
    void testFindNearestAvailableAmbulance() {
        Ambulance nearest = ambulanceService.findNearestAvailableAmbulance(12.9710, 77.5940, AmbulanceType.ICU);
        assertNotNull(nearest, "Nearest available ICU ambulance should be found");
        assertEquals(AmbulanceType.ICU, nearest.getAmbulanceType());
    }

    @Test
    @Transactional
    void testCreateBookingAndNearestAllocation() {
        BookingRequest request = new BookingRequest();
        request.setPatientName("Alice Test");
        request.setPatientPhone("+1-800-555-9999");
        request.setPickupAddress("Test Location, MG Road");
        request.setPickupLatitude(12.9720);
        request.setPickupLongitude(77.5950);
        request.setDestinationAddress("City Emergency Hospital");
        request.setDestinationLatitude(12.97244);
        request.setDestinationLongitude(77.58064);
        request.setEmergencyType(EmergencyType.CRITICAL);
        request.setAmbulanceType(AmbulanceType.ICU);
        request.setNotes("Severe chest pain test");

        BookingDTO booking = bookingService.createBooking(request);

        assertNotNull(booking);
        assertNotNull(booking.getBookingCode());
        assertEquals(BookingStatus.ASSIGNED, booking.getBookingStatus());
        assertNotNull(booking.getAmbulanceId());
        assertNotNull(booking.getDriverId());
    }

    @Test
    void testHospitalSearch() {
        List<HospitalDTO> nearby = hospitalService.getNearbyHospitals(12.9716, 77.5946);
        assertFalse(nearby.isEmpty(), "Hospitals list should not be empty");
        assertNotNull(nearby.get(0).getDistanceKm(), "First hospital should have distance calculated");
    }

    @Test
    @Transactional
    void testTrackingLocationUpdate() {
        Ambulance amb = ambulanceRepository.findAll().get(0);
        LocationPingRequest ping = new LocationPingRequest(amb.getId(), null, 12.9750, 77.5980, 42.0);

        TrackingDTO tracking = trackingService.recordLocation(ping);
        assertNotNull(tracking);
        assertEquals(12.9750, tracking.getLatitude());
        assertEquals(77.5980, tracking.getLongitude());
    }
}
