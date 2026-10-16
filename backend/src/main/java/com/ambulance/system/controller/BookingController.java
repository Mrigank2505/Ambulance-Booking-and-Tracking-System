package com.ambulance.system.controller;

import com.ambulance.system.dto.ApiResponse;
import com.ambulance.system.dto.BookingDTO;
import com.ambulance.system.dto.BookingRequest;
import com.ambulance.system.dto.BookingStatusUpdateRequest;
import com.ambulance.system.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Ambulance emergency dispatch booking and status lifecycle APIs")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create an emergency ambulance booking request")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingDTO booking = bookingService.createBooking(request);
        return new ResponseEntity<>(ApiResponse.ok("Booking request created", booking), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get bookings for the currently authenticated user")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getCurrentUserBookings() {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getCurrentUserBookings()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBookingById(id)));
    }

    @GetMapping("/code/{bookingCode}")
    @Operation(summary = "Get booking details by unique booking code")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingByCode(@PathVariable String bookingCode) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBookingByCode(bookingCode)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get bookings by user ID")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBookingsByUserId(userId)));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @PathVariable Long id, @RequestParam(required = false) String reason) {
        BookingDTO cancelled = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Booking cancelled", cancelled));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update booking status with validation")
    public ResponseEntity<ApiResponse<BookingDTO>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody BookingStatusUpdateRequest request) {
        BookingDTO updated = bookingService.updateBookingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Status updated", updated));
    }
}
