package com.ambulance.system.controller;

import com.ambulance.system.dto.ApiResponse;
import com.ambulance.system.dto.LocationPingRequest;
import com.ambulance.system.dto.TrackingDTO;
import com.ambulance.system.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@Tag(name = "Tracking", description = "Real-time GPS tracking and DEMO simulation endpoints")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/location")
    @Operation(summary = "Submit an ambulance location GPS ping and broadcast to WebSocket")
    public ResponseEntity<ApiResponse<TrackingDTO>> recordLocation(@Valid @RequestBody LocationPingRequest request) {
        TrackingDTO tracking = trackingService.recordLocation(request);
        return ResponseEntity.ok(ApiResponse.ok("Location recorded and broadcasted", tracking));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get latest live tracking telemetry for a booking")
    public ResponseEntity<ApiResponse<TrackingDTO>> getTracking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.ok(trackingService.getLatestTrackingForBooking(bookingId)));
    }

    @GetMapping("/{bookingId}/latest")
    @Operation(summary = "Alias for getting latest live tracking telemetry")
    public ResponseEntity<ApiResponse<TrackingDTO>> getLatestTracking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.ok(trackingService.getLatestTrackingForBooking(bookingId)));
    }

    @GetMapping("/{bookingId}/history")
    @Operation(summary = "Get route history breadcrumbs for a booking")
    public ResponseEntity<ApiResponse<List<TrackingDTO>>> getLocationHistory(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.ok(trackingService.getLocationHistoryForBooking(bookingId)));
    }

    @PostMapping("/{bookingId}/demo-step")
    @Operation(summary = "Simulate a DEMO mode movement step toward patient or hospital")
    public ResponseEntity<ApiResponse<TrackingDTO>> simulateDemoStep(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "0.15") double stepRatio) {
        TrackingDTO tracking = trackingService.simulateDemoStep(bookingId, stepRatio);
        return ResponseEntity.ok(ApiResponse.ok("Demo step simulated", tracking));
    }
}
