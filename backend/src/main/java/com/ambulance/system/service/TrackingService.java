package com.ambulance.system.service;

import com.ambulance.system.dto.LocationPingRequest;
import com.ambulance.system.dto.TrackingDTO;

import java.util.List;

public interface TrackingService {
    TrackingDTO recordLocation(LocationPingRequest request);
    TrackingDTO getLatestTrackingForBooking(Long bookingId);
    TrackingDTO getLatestTrackingForAmbulance(Long ambulanceId);
    List<TrackingDTO> getLocationHistoryForBooking(Long bookingId);
    TrackingDTO simulateDemoStep(Long bookingId, double stepRatio);
}
