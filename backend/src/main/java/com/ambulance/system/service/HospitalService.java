package com.ambulance.system.service;

import com.ambulance.system.dto.HospitalCreateRequest;
import com.ambulance.system.dto.HospitalDTO;

import java.util.List;

public interface HospitalService {
    List<HospitalDTO> getAllHospitals();
    List<HospitalDTO> getNearbyHospitals(Double userLat, Double userLon);
    List<HospitalDTO> getNearbyHospitals(Double userLat, Double userLon, Double radiusKm);
    HospitalDTO getHospitalById(Long id);
    HospitalDTO createHospital(HospitalCreateRequest request);
    HospitalDTO updateHospital(Long id, HospitalCreateRequest request);
    void deleteHospital(Long id);
}
