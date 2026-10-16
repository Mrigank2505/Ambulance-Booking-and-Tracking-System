package com.ambulance.system.controller;

import com.ambulance.system.dto.*;
import com.ambulance.system.entity.Role;
import com.ambulance.system.entity.UserStatus;
import com.ambulance.system.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "System administrator oversight, fleet, user management, and statistics APIs")
public class AdminController {

    private final AdminService adminService;
    private final DriverService driverService;
    private final AmbulanceService ambulanceService;
    private final HospitalService hospitalService;
    private final BookingService bookingService;

    public AdminController(AdminService adminService,
                           DriverService driverService,
                           AmbulanceService ambulanceService,
                           HospitalService hospitalService,
                           BookingService bookingService) {
        this.adminService = adminService;
        this.driverService = driverService;
        this.ambulanceService = ambulanceService;
        this.hospitalService = hospitalService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get high-level system dashboard analytics and KPIs")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    @GetMapping("/users")
    @Operation(summary = "Admin: Get all users in the system")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Admin: Update user account status (ACTIVE, INACTIVE, SUSPENDED)")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable Long id, @RequestParam UserStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("User status updated", adminService.updateUserStatus(id, status)));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Admin: Update user role")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(
            @PathVariable Long id, @RequestParam Role role) {
        return ResponseEntity.ok(ApiResponse.ok("User role updated", adminService.updateUserRole(id, role)));
    }

    @GetMapping("/drivers")
    @Operation(summary = "Admin: Get all drivers")
    public ResponseEntity<ApiResponse<List<DriverDTO>>> getAllDrivers() {
        return ResponseEntity.ok(ApiResponse.ok(driverService.getAllDrivers()));
    }

    @GetMapping("/ambulances")
    @Operation(summary = "Admin: Get all ambulances")
    public ResponseEntity<ApiResponse<List<AmbulanceDTO>>> getAllAmbulances() {
        return ResponseEntity.ok(ApiResponse.ok(ambulanceService.getAllAmbulances()));
    }

    @GetMapping("/hospitals")
    @Operation(summary = "Admin: Get all hospitals")
    public ResponseEntity<ApiResponse<List<HospitalDTO>>> getAllHospitals() {
        return ResponseEntity.ok(ApiResponse.ok(hospitalService.getAllHospitals()));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Admin: Get all bookings across the platform")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getAllBookings()));
    }
}
