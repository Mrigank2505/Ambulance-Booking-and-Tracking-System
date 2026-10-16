package com.ambulance.system.controller;

import com.ambulance.system.dto.AmbulanceCreateRequest;
import com.ambulance.system.dto.AmbulanceDTO;
import com.ambulance.system.dto.ApiResponse;
import com.ambulance.system.entity.AmbulanceType;
import com.ambulance.system.service.AmbulanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
@Tag(name = "Ambulances", description = "Ambulance management and availability lookup endpoints")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    public AmbulanceController(AmbulanceService ambulanceService) {
        this.ambulanceService = ambulanceService;
    }

    @GetMapping
    @Operation(summary = "Get list of all ambulances")
    public ResponseEntity<ApiResponse<List<AmbulanceDTO>>> getAllAmbulances() {
        return ResponseEntity.ok(ApiResponse.ok(ambulanceService.getAllAmbulances()));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available ambulances with distance and ETA calculation")
    public ResponseEntity<ApiResponse<List<AmbulanceDTO>>> getAvailableAmbulances(
            @RequestParam(required = false) AmbulanceType type,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        return ResponseEntity.ok(ApiResponse.ok(ambulanceService.getAvailableAmbulances(type, lat, lon)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single ambulance by ID")
    public ResponseEntity<ApiResponse<AmbulanceDTO>> getAmbulanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ambulanceService.getAmbulanceById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create/Register a new ambulance into fleet")
    public ResponseEntity<ApiResponse<AmbulanceDTO>> createAmbulance(@Valid @RequestBody AmbulanceCreateRequest request) {
        AmbulanceDTO created = ambulanceService.createAmbulance(request);
        return new ResponseEntity<>(ApiResponse.ok("Ambulance created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update ambulance fleet details")
    public ResponseEntity<ApiResponse<AmbulanceDTO>> updateAmbulance(
            @PathVariable Long id, @Valid @RequestBody AmbulanceCreateRequest request) {
        AmbulanceDTO updated = ambulanceService.updateAmbulance(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Ambulance updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete ambulance from fleet")
    public ResponseEntity<ApiResponse<Void>> deleteAmbulance(@PathVariable Long id) {
        ambulanceService.deleteAmbulance(id);
        return ResponseEntity.ok(ApiResponse.ok("Ambulance deleted successfully", null));
    }
}
