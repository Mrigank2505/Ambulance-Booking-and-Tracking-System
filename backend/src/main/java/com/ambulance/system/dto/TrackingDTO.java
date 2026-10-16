package com.ambulance.system.dto;

import com.ambulance.system.entity.BookingStatus;
import java.time.LocalDateTime;

public class TrackingDTO {
    private Long ambulanceId;
    private Long bookingId;
    private String bookingCode;
    private String ambulanceNumber;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private LocalDateTime timestamp;
    private String eta;
    private Double distanceRemainingKm;
    private BookingStatus status;
    private String driverName;
    private String driverPhone;

    public TrackingDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public TrackingDTO(Long ambulanceId, Long bookingId, String bookingCode, String ambulanceNumber,
                       Double latitude, Double longitude, Double speed, LocalDateTime timestamp,
                       String eta, Double distanceRemainingKm, BookingStatus status,
                       String driverName, String driverPhone) {
        this.ambulanceId = ambulanceId;
        this.bookingId = bookingId;
        this.bookingCode = bookingCode;
        this.ambulanceNumber = ambulanceNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.eta = eta;
        this.distanceRemainingKm = distanceRemainingKm;
        this.status = status;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
    }

    public Long getAmbulanceId() {
        return ambulanceId;
    }

    public void setAmbulanceId(Long ambulanceId) {
        this.ambulanceId = ambulanceId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public String getAmbulanceNumber() {
        return ambulanceNumber;
    }

    public void setAmbulanceNumber(String ambulanceNumber) {
        this.ambulanceNumber = ambulanceNumber;
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

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public Double getDistanceRemainingKm() {
        return distanceRemainingKm;
    }

    public void setDistanceRemainingKm(Double distanceRemainingKm) {
        this.distanceRemainingKm = distanceRemainingKm;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
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
}
