package com.ambulance.system.service;

import com.ambulance.system.dto.BookingDTO;
import com.ambulance.system.dto.DriverAvailabilityRequest;
import com.ambulance.system.dto.DriverDTO;
import com.ambulance.system.dto.LocationUpdateRequest;

import java.util.List;

public interface DriverService {
    List<DriverDTO> getAllDrivers();
    DriverDTO getDriverById(Long id);
    DriverDTO getCurrentDriver();
    DriverDTO updateAvailability(Long driverId, DriverAvailabilityRequest request);
    DriverDTO updateCurrentDriverAvailability(DriverAvailabilityRequest request);
    DriverDTO updateLocation(Long driverId, LocationUpdateRequest request);
    DriverDTO updateCurrentDriverLocation(LocationUpdateRequest request);
    List<BookingDTO> getPendingBookingsForCurrentDriver();
    List<BookingDTO> getActiveBookingsForCurrentDriver();
}
