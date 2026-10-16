package com.ambulance.system.dto;

import com.ambulance.system.entity.DriverAvailability;
import jakarta.validation.constraints.NotNull;

public class DriverAvailabilityRequest {

    @NotNull(message = "Availability status is required")
    private DriverAvailability availabilityStatus;

    public DriverAvailabilityRequest() {
    }

    public DriverAvailabilityRequest(DriverAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public DriverAvailability getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(DriverAvailability availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }
}
