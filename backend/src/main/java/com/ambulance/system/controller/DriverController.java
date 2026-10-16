package com.ambulance.system.controller;

import com.ambulance.system.dto.*;
import com.ambulance.system.service.BookingService;
import com.ambulance.system.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Drivers", description = "Driver tracking, availability, and trip lifecycle APIs")
public class DriverController {

    private final DriverService driverService;
    private final BookingService bookingService;

    public DriverController(DriverService driverService, BookingService bookingService) {
        this.driverService = driverService;
        this.bookingService = bookingService;
    }

    // --- General driver queries ---
    @GetMapping("/api/drivers")
    @Operation(summary = "Get all drivers")
    public ResponseEntity<ApiResponse<List<DriverDTO>>> getAllDrivers() {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getAllDrivers()));
    }

    @GetMapping("/api/drivers/{id}")
    @Operation(summary = "Get single driver by ID")
    public ResponseEntity<ApiResponse<DriverDTO>> getDriverById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getDriverById(id)));
    }

    @PutMapping("/api/drivers/{id}/availability")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Update driver availability status by driver ID")
    public ResponseEntity<ApiResponse<DriverDTO>> updateDriverAvailability(
            @PathVariable Long id, @Valid @RequestBody DriverAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateAvailability(id, request)));
    }

    @PutMapping("/api/drivers/{id}/location")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Update driver current location by driver ID")
    public ResponseEntity<ApiResponse<DriverDTO>> updateDriverLocation(
            @PathVariable Long id, @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateLocation(id, request)));
    }

    // --- Current logged in driver actions ---
    @GetMapping("/api/driver/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get logged in driver's own profile")
    public ResponseEntity<ApiResponse<DriverDTO>> getCurrentDriver() {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getCurrentDriver()));
    }

    @PutMapping("/api/driver/availability")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Toggle online/offline status for logged-in driver")
    public ResponseEntity<ApiResponse<DriverDTO>> updateMyAvailability(@Valid @RequestBody DriverAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateCurrentDriverAvailability(request)));
    }

    @PutMapping("/api/driver/location")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Update location coordinates for logged-in driver")
    public ResponseEntity<ApiResponse<DriverDTO>> updateMyLocation(@Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(driverService.updateCurrentDriverLocation(request)));
    }

    @GetMapping("/api/driver/bookings/pending")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get pending trip requests waiting for driver acceptance")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getPendingBookings() {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getPendingBookingsForCurrentDriver()));
    }

    @GetMapping("/api/driver/bookings/active")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Get active ongoing trips for driver")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getActiveBookings() {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getActiveBookingsForCurrentDriver()));
    }

    @PutMapping("/api/driver/bookings/{id}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver accepts assigned emergency trip request")
    public ResponseEntity<ApiResponse<BookingDTO>> acceptBooking(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Booking accepted", bookingService.driverAcceptBooking(id)));
    }

    @PutMapping("/api/driver/bookings/{id}/reject")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver rejects assigned emergency trip request")
    public ResponseEntity<ApiResponse<BookingDTO>> rejectBooking(
            @PathVariable Long id, @RequestParam(required = false, defaultValue = "Driver unavailable") String reason) {
        return ResponseEntity.ok(ApiResponse.ok("Booking rejected", bookingService.driverRejectBooking(id, reason)));
    }

    @PutMapping("/api/driver/bookings/{id}/arrived")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver marks arrival at patient pickup address")
    public ResponseEntity<ApiResponse<BookingDTO>> markArrived(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Ambulance marked arrived", bookingService.driverMarkArrived(id)));
    }

    @PutMapping("/api/driver/bookings/{id}/picked-up")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver marks patient picked up and en-route to hospital")
    public ResponseEntity<ApiResponse<BookingDTO>> markPickedUp(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Patient picked up", bookingService.driverMarkPickedUp(id)));
    }

    @PutMapping("/api/driver/bookings/{id}/completed")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Driver marks trip completed at destination hospital")
    public ResponseEntity<ApiResponse<BookingDTO>> markCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Trip completed successfully", bookingService.driverMarkCompleted(id)));
    }
}
