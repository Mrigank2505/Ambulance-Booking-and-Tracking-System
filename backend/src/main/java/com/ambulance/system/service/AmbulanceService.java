package com.ambulance.system.service;

import com.ambulance.system.dto.AmbulanceCreateRequest;
import com.ambulance.system.dto.AmbulanceDTO;
import com.ambulance.system.entity.Ambulance;
import com.ambulance.system.entity.AmbulanceType;

import java.util.List;

public interface AmbulanceService {
    List<AmbulanceDTO> getAllAmbulances();
    List<AmbulanceDTO> getAvailableAmbulances(AmbulanceType type, Double userLat, Double userLon);
    AmbulanceDTO getAmbulanceById(Long id);
    AmbulanceDTO createAmbulance(AmbulanceCreateRequest request);
    AmbulanceDTO updateAmbulance(Long id, AmbulanceCreateRequest request);
    void deleteAmbulance(Long id);
    Ambulance findNearestAvailableAmbulance(double pickupLat, double pickupLon, AmbulanceType requestedType);
}
