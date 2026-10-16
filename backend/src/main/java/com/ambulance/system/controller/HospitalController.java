package com.ambulance.system.controller;

import com.ambulance.system.dto.ApiResponse;
import com.ambulance.system.dto.HospitalCreateRequest;
import com.ambulance.system.dto.HospitalDTO;
import com.ambulance.system.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@Tag(name = "Hospitals", description = "Hospital lookup, trauma capabilities, and emergency center APIs")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    @Operation(summary = "Get list of all hospitals")
    public ResponseEntity<ApiResponse<List<HospitalDTO>>> getAllHospitals() {
        return ResponseEntity.ok(ApiResponse.ok(hospitalService.getAllHospitals()));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby hospitals within a radius (default 10 km) sorted by distance")
    public ResponseEntity<ApiResponse<List<HospitalDTO>>> getNearbyHospitals(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10.0") Double radiusKm) {
        Double targetLat = lat != null ? lat : latitude;
        Double targetLon = lon != null ? lon : longitude;
        return ResponseEntity.ok(ApiResponse.ok(hospitalService.getNearbyHospitals(targetLat, targetLon, radiusKm)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single hospital details by ID")
    public ResponseEntity<ApiResponse<HospitalDTO>> getHospitalById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(hospitalService.getHospitalById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new hospital facility")
    public ResponseEntity<ApiResponse<HospitalDTO>> createHospital(@Valid @RequestBody HospitalCreateRequest request) {
        HospitalDTO created = hospitalService.createHospital(request);
        return new ResponseEntity<>(ApiResponse.ok("Hospital added successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update hospital information")
    public ResponseEntity<ApiResponse<HospitalDTO>> updateHospital(
            @PathVariable Long id, @Valid @RequestBody HospitalCreateRequest request) {
        HospitalDTO updated = hospitalService.updateHospital(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Hospital updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete hospital record")
    public ResponseEntity<ApiResponse<Void>> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.ok(ApiResponse.ok("Hospital deleted successfully", null));
    }
}
