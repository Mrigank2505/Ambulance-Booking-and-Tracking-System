package com.ambulance.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ambulances")
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambulance_number", nullable = false, unique = true, length = 50)
    private String ambulanceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "ambulance_type", nullable = false, length = 20)
    private AmbulanceType ambulanceType = AmbulanceType.BASIC;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", unique = true)
    private Driver driver;

    @Column(nullable = false)
    private Integer capacity = 1;

    @Column(name = "equipment_details", columnDefinition = "TEXT")
    private String equipmentDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    private AmbulanceAvailability availabilityStatus = AmbulanceAvailability.AVAILABLE;

    @Column(name = "current_latitude", nullable = false)
    private Double currentLatitude = 0.0;

    @Column(name = "current_longitude", nullable = false)
    private Double currentLongitude = 0.0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Ambulance() {
    }

    public Ambulance(Long id, String ambulanceNumber, AmbulanceType ambulanceType, Driver driver,
                     Integer capacity, String equipmentDetails, AmbulanceAvailability availabilityStatus,
                     Double currentLatitude, Double currentLongitude) {
        this.id = id;
        this.ambulanceNumber = ambulanceNumber;
        this.ambulanceType = ambulanceType != null ? ambulanceType : AmbulanceType.BASIC;
        this.driver = driver;
        this.capacity = capacity != null ? capacity : 1;
        this.equipmentDetails = equipmentDetails;
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : AmbulanceAvailability.AVAILABLE;
        this.currentLatitude = currentLatitude != null ? currentLatitude : 0.0;
        this.currentLongitude = currentLongitude != null ? currentLongitude : 0.0;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
