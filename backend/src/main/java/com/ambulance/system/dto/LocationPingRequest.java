package com.ambulance.system.dto;

import jakarta.validation.constraints.NotNull;

public class LocationPingRequest {

    @NotNull(message = "Ambulance ID is required")
    private Long ambulanceId;

    private Long bookingId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double speed = 0.0;

    public LocationPingRequest() {
    }

    public LocationPingRequest(Long ambulanceId, Long bookingId, Double latitude, Double longitude, Double speed) {
        this.ambulanceId = ambulanceId;
        this.bookingId = bookingId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed != null ? speed : 0.0;
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
}
