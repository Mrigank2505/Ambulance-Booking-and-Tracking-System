package com.ambulance.system.service.impl;

import com.ambulance.system.dto.HospitalCreateRequest;
import com.ambulance.system.dto.HospitalDTO;
import com.ambulance.system.entity.Hospital;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.BookingRepository;
import com.ambulance.system.repository.HospitalRepository;
import com.ambulance.system.service.HospitalService;
import com.ambulance.system.util.DistanceUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final BookingRepository bookingRepository;
    private final DistanceUtil distanceUtil;
    private final EntityDtoMapper mapper;

    public HospitalServiceImpl(HospitalRepository hospitalRepository,
                               com.ambulance.system.repository.BookingRepository bookingRepository,
                               DistanceUtil distanceUtil,
                               EntityDtoMapper mapper) {
        this.hospitalRepository = hospitalRepository;
        this.bookingRepository = bookingRepository;
        this.distanceUtil = distanceUtil;
        this.mapper = mapper;
    }

    @Override
    public List<HospitalDTO> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(mapper::toHospitalDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HospitalDTO> getNearbyHospitals(Double userLat, Double userLon) {
        return getNearbyHospitals(userLat, userLon, 10.0);
    }

    @Override
    public List<HospitalDTO> getNearbyHospitals(Double userLat, Double userLon, Double radiusKm) {
        if (userLat == null || userLon == null) {
            return java.util.Collections.emptyList();
        }

        // Validate coordinates: latitude must be [-90, 90] and longitude must be [-180, 180]
        if (Double.isNaN(userLat) || Double.isNaN(userLon) ||
                userLat < -90.0 || userLat > 90.0 || userLon < -180.0 || userLon > 180.0) {
            return java.util.Collections.emptyList();
        }

        double maxRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : 10.0;

        return hospitalRepository.findAll().stream()
                .filter(h -> h.getLatitude() != null && h.getLongitude() != null)
                .map(h -> {
                    HospitalDTO dto = mapper.toHospitalDTO(h);
                    double dist = distanceUtil.calculateDistance(userLat, userLon, h.getLatitude(), h.getLongitude());
                    dto.setDistanceKm(dist);
                    return dto;
                })
                .filter(dto -> dto.getDistanceKm() != null && dto.getDistanceKm() <= maxRadius)
                .sorted(Comparator.comparingDouble(HospitalDTO::getDistanceKm))
                .collect(Collectors.toList());
    }

    @Override
    public HospitalDTO getHospitalById(Long id) {
        Hospital h = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        return mapper.toHospitalDTO(h);
    }

    @Override
    @Transactional
    public HospitalDTO createHospital(HospitalCreateRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.getName().trim());
        hospital.setAddress(request.getAddress().trim());
        hospital.setPhone(request.getPhone().trim());
        hospital.setLatitude(request.getLatitude());
        hospital.setLongitude(request.getLongitude());
        hospital.setEmergencyAvailable(request.getEmergencyAvailable() != null ? request.getEmergencyAvailable() : true);
        hospital.setIcuAvailable(request.getIcuAvailable() != null ? request.getIcuAvailable() : true);
        hospital.setDescription(request.getDescription());

        Hospital saved = hospitalRepository.save(hospital);
        return mapper.toHospitalDTO(saved);
    }

    @Override
    @Transactional
    public HospitalDTO updateHospital(Long id, HospitalCreateRequest request) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));

        hospital.setName(request.getName().trim());
        hospital.setAddress(request.getAddress().trim());
        hospital.setPhone(request.getPhone().trim());
        hospital.setLatitude(request.getLatitude());
        hospital.setLongitude(request.getLongitude());
        if (request.getEmergencyAvailable() != null) {
            hospital.setEmergencyAvailable(request.getEmergencyAvailable());
        }
        if (request.getIcuAvailable() != null) {
            hospital.setIcuAvailable(request.getIcuAvailable());
        }
        hospital.setDescription(request.getDescription());

        Hospital updated = hospitalRepository.save(hospital);
        return mapper.toHospitalDTO(updated);
    }

    @Override
    @Transactional
    public void deleteHospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
        if (bookingRepository.existsByHospitalId(id)) {
            hospital.setEmergencyAvailable(false);
            hospital.setIcuAvailable(false);
            hospitalRepository.save(hospital);
        } else {
            hospitalRepository.delete(hospital);
        }
    }
}
