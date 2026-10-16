package com.ambulance.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HospitalCreateRequest {

    @NotBlank(message = "Hospital name is required")
    private String name;

    @NotBlank(message = "Hospital address is required")
    private String address;

    @NotBlank(message = "Contact phone is required")
    private String phone;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Boolean emergencyAvailable = true;
    private Boolean icuAvailable = true;
    private String description;

    public HospitalCreateRequest() {
    }

    public HospitalCreateRequest(String name, String address, String phone, Double latitude, Double longitude,
                                 Boolean emergencyAvailable, Boolean icuAvailable, String description) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.emergencyAvailable = emergencyAvailable != null ? emergencyAvailable : true;
        this.icuAvailable = icuAvailable != null ? icuAvailable : true;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getEmergencyAvailable() {
        return emergencyAvailable;
    }

    public void setEmergencyAvailable(Boolean emergencyAvailable) {
        this.emergencyAvailable = emergencyAvailable;
    }

    public Boolean getIcuAvailable() {
        return icuAvailable;
    }

    public void setIcuAvailable(Boolean icuAvailable) {
        this.icuAvailable = icuAvailable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
