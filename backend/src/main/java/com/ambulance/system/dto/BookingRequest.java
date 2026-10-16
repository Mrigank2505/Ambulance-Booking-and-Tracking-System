package com.ambulance.system.dto;

import com.ambulance.system.entity.AmbulanceType;
import com.ambulance.system.entity.EmergencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotBlank(message = "Patient phone is required")
    private String patientPhone;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Long hospitalId;

    @NotNull(message = "Emergency severity type is required")
    private EmergencyType emergencyType = EmergencyType.NORMAL;

    @NotNull(message = "Ambulance type is required")
    private AmbulanceType ambulanceType = AmbulanceType.BASIC;

    private String notes;

    public BookingRequest() {
    }

    public BookingRequest(String patientName, String patientPhone, String pickupAddress, Double pickupLatitude,
                          Double pickupLongitude, String destinationAddress, Double destinationLatitude,
                          Double destinationLongitude, Long hospitalId, EmergencyType emergencyType,
                          AmbulanceType ambulanceType, String notes) {
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.pickupAddress = pickupAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.destinationAddress = destinationAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.hospitalId = hospitalId;
        this.emergencyType = emergencyType != null ? emergencyType : EmergencyType.NORMAL;
        this.ambulanceType = ambulanceType != null ? ambulanceType : AmbulanceType.BASIC;
        this.notes = notes;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(Double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(Double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public Long getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    public EmergencyType getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(EmergencyType emergencyType) {
        this.emergencyType = emergencyType;
    }

    public AmbulanceType getAmbulanceType() {
        return ambulanceType;
    }

    public void setAmbulanceType(AmbulanceType ambulanceType) {
        this.ambulanceType = ambulanceType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
