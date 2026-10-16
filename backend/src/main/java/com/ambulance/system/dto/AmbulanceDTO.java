package com.ambulance.system.dto;

import com.ambulance.system.entity.AmbulanceAvailability;
import com.ambulance.system.entity.AmbulanceType;

public class AmbulanceDTO {
    private Long id;
    private String ambulanceNumber;
    private AmbulanceType ambulanceType;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Integer capacity;
    private String equipmentDetails;
    private AmbulanceAvailability availabilityStatus;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double distanceKm;
    private Integer etaMinutes;

    public AmbulanceDTO() {
    }

    public AmbulanceDTO(Long id, String ambulanceNumber, AmbulanceType ambulanceType, Long driverId,
                        String driverName, String driverPhone, Integer capacity, String equipmentDetails,
                        AmbulanceAvailability availabilityStatus, Double currentLatitude, Double currentLongitude,
                        Double distanceKm, Integer etaMinutes) {
        this.id = id;
        this.ambulanceNumber = ambulanceNumber;
        this.ambulanceType = ambulanceType;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.capacity = capacity;
        this.equipmentDetails = equipmentDetails;
        this.availabilityStatus = availabilityStatus;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.distanceKm = distanceKm;
        this.etaMinutes = etaMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(Integer etaMinutes) {
        this.etaMinutes = etaMinutes;
    }
}
