package com.ambulance.system.controller;

import com.ambulance.system.dto.*;
import com.ambulance.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and emergency contact management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user's full profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUserProfile()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update authenticated user's profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        UserDTO updated = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @GetMapping("/me/contacts")
    @Operation(summary = "List emergency contacts for user")
    public ResponseEntity<ApiResponse<List<EmergencyContactDTO>>> getEmergencyContacts() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getEmergencyContacts()));
    }

    @PostMapping("/me/contacts")
    @Operation(summary = "Add new emergency contact")
    public ResponseEntity<ApiResponse<EmergencyContactDTO>> addEmergencyContact(@Valid @RequestBody EmergencyContactDTO request) {
        EmergencyContactDTO created = userService.addEmergencyContact(request);
        return new ResponseEntity<>(ApiResponse.ok("Emergency contact added", created), HttpStatus.CREATED);
    }

    @DeleteMapping("/me/contacts/{id}")
    @Operation(summary = "Delete an emergency contact")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(@PathVariable Long id) {
        userService.deleteEmergencyContact(id);
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact deleted", null));
    }
}
