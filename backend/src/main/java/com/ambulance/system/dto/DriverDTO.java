package com.ambulance.system.dto;

import com.ambulance.system.entity.DriverAvailability;

public class DriverDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String licenseNumber;
    private DriverAvailability availabilityStatus;
    private Double currentLatitude;
    private Double currentLongitude;
    private String assignedAmbulanceNumber;

    public DriverDTO() {
    }

    public DriverDTO(Long id, Long userId, String fullName, String email, String phone, String licenseNumber,
                     DriverAvailability availabilityStatus, Double currentLatitude, Double currentLongitude,
                     String assignedAmbulanceNumber) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.availabilityStatus = availabilityStatus;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.assignedAmbulanceNumber = assignedAmbulanceNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public DriverAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(DriverAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public String getAssignedAmbulanceNumber() {
        return assignedAmbulanceNumber;
    }

    public void setAssignedAmbulanceNumber(String assignedAmbulanceNumber) {
        this.assignedAmbulanceNumber = assignedAmbulanceNumber;
    }
}
