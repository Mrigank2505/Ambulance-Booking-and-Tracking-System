package com.ambulance.system.dto;

import com.ambulance.system.entity.AmbulanceAvailability;
import com.ambulance.system.entity.AmbulanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AmbulanceCreateRequest {

    @NotBlank(message = "Ambulance number is required")
    private String ambulanceNumber;

    @NotNull(message = "Ambulance type is required")
    private AmbulanceType ambulanceType;

    private Long driverId;

    private Integer capacity = 1;

    private String equipmentDetails;

    private AmbulanceAvailability availabilityStatus = AmbulanceAvailability.AVAILABLE;

    @NotNull(message = "Latitude is required")
    private Double currentLatitude;

    @NotNull(message = "Longitude is required")
    private Double currentLongitude;

    public AmbulanceCreateRequest() {
    }

    public AmbulanceCreateRequest(String ambulanceNumber, AmbulanceType ambulanceType, Long driverId, Integer capacity,
                                  String equipmentDetails, AmbulanceAvailability availabilityStatus,
                                  Double currentLatitude, Double currentLongitude) {
        this.ambulanceNumber = ambulanceNumber;
        this.ambulanceType = ambulanceType;
        this.driverId = driverId;
        this.capacity = capacity;
        this.equipmentDetails = equipmentDetails;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : AmbulanceAvailability.AVAILABLE;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    public String getAmbulanceNumber() {
        return ambulanceNumber;
    }

    public void setAmbulanceNumber(String ambulanceNumber) {
        this.ambulanceNumber = ambulanceNumber;
    }

    public AmbulanceType getAmbulanceType() {
        return ambulanceType;
    }

    public void setAmbulanceType(AmbulanceType ambulanceType) {
        this.ambulanceType = ambulanceType;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getEquipmentDetails() {
        return equipmentDetails;
    }

    public void setEquipmentDetails(String equipmentDetails) {
        this.equipmentDetails = equipmentDetails;
    }

    public AmbulanceAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AmbulanceAvailability availabilityStatus) {
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
}
