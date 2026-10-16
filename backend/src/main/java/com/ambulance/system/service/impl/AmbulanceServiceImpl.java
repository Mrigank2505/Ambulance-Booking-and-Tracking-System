package com.ambulance.system.service.impl;

import com.ambulance.system.dto.AmbulanceCreateRequest;
import com.ambulance.system.dto.AmbulanceDTO;
import com.ambulance.system.entity.Ambulance;
import com.ambulance.system.entity.AmbulanceAvailability;
import com.ambulance.system.entity.AmbulanceType;
import com.ambulance.system.entity.Driver;
import com.ambulance.system.exception.BadRequestException;
import com.ambulance.system.exception.ResourceNotFoundException;
import com.ambulance.system.mapper.EntityDtoMapper;
import com.ambulance.system.repository.AmbulanceRepository;
import com.ambulance.system.repository.BookingRepository;
import com.ambulance.system.repository.DriverRepository;
import com.ambulance.system.service.AmbulanceService;
import com.ambulance.system.util.DistanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmbulanceServiceImpl implements AmbulanceService {

    private static final Logger log = LoggerFactory.getLogger(AmbulanceServiceImpl.class);

    private final AmbulanceRepository ambulanceRepository;
    private final DriverRepository driverRepository;
    private final BookingRepository bookingRepository;
    private final DistanceUtil distanceUtil;
    private final EntityDtoMapper mapper;

    public AmbulanceServiceImpl(AmbulanceRepository ambulanceRepository,
                                DriverRepository driverRepository,
                                BookingRepository bookingRepository,
                                DistanceUtil distanceUtil,
                                EntityDtoMapper mapper) {
        this.ambulanceRepository = ambulanceRepository;
        this.driverRepository = driverRepository;
        this.bookingRepository = bookingRepository;
        this.distanceUtil = distanceUtil;
        this.mapper = mapper;
    }

    @Override
    public List<AmbulanceDTO> getAllAmbulances() {
        return ambulanceRepository.findAll().stream()
                .map(mapper::toAmbulanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AmbulanceDTO> getAvailableAmbulances(AmbulanceType type, Double userLat, Double userLon) {
        List<Ambulance> list;
        if (type != null) {
            list = ambulanceRepository.findByAmbulanceTypeAndAvailabilityStatus(type, AmbulanceAvailability.AVAILABLE);
        } else {
            list = ambulanceRepository.findByAvailabilityStatus(AmbulanceAvailability.AVAILABLE);
        }

        return list.stream().map(amb -> {
            AmbulanceDTO dto = mapper.toAmbulanceDTO(amb);
            if (userLat != null && userLon != null) {
                double dist = distanceUtil.calculateDistance(userLat, userLon, amb.getCurrentLatitude(), amb.getCurrentLongitude());
                dto.setDistanceKm(dist);
                dto.setEtaMinutes(distanceUtil.estimateEtaMinutes(dist));
            }
            return dto;
        }).sorted((a, b) -> {
            if (a.getDistanceKm() != null && b.getDistanceKm() != null) {
                return Double.compare(a.getDistanceKm(), b.getDistanceKm());
            }
            return 0;
        }).collect(Collectors.toList());
    }

    @Override
    public AmbulanceDTO getAmbulanceById(Long id) {
        Ambulance amb = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with id: " + id));
        return mapper.toAmbulanceDTO(amb);
    }

    @Override
    @Transactional
    public AmbulanceDTO createAmbulance(AmbulanceCreateRequest request) {
        if (ambulanceRepository.findByAmbulanceNumber(request.getAmbulanceNumber()).isPresent()) {
            throw new BadRequestException("Ambulance with number " + request.getAmbulanceNumber() + " already exists");
        }

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + request.getDriverId()));
        }

        Ambulance ambulance = new Ambulance();
        ambulance.setAmbulanceNumber(request.getAmbulanceNumber().trim().toUpperCase());
        ambulance.setAmbulanceType(request.getAmbulanceType());
        ambulance.setDriver(driver);
        ambulance.setCapacity(request.getCapacity() != null ? request.getCapacity() : 1);
        ambulance.setEquipmentDetails(request.getEquipmentDetails());
        ambulance.setAvailabilityStatus(request.getAvailabilityStatus() != null ? request.getAvailabilityStatus() : AmbulanceAvailability.AVAILABLE);
        ambulance.setCurrentLatitude(request.getCurrentLatitude());
        ambulance.setCurrentLongitude(request.getCurrentLongitude());

        Ambulance saved = ambulanceRepository.save(ambulance);
        log.info("Registered new ambulance: {}", saved.getAmbulanceNumber());
        return mapper.toAmbulanceDTO(saved);
    }

    @Override
    @Transactional
    public AmbulanceDTO updateAmbulance(Long id, AmbulanceCreateRequest request) {
        Ambulance ambulance = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with id: " + id));

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + request.getDriverId()));
        }

        ambulance.setAmbulanceNumber(request.getAmbulanceNumber().trim().toUpperCase());
        ambulance.setAmbulanceType(request.getAmbulanceType());
        ambulance.setDriver(driver);
        ambulance.setCapacity(request.getCapacity() != null ? request.getCapacity() : 1);
        ambulance.setEquipmentDetails(request.getEquipmentDetails());
        if (request.getAvailabilityStatus() != null) {
            ambulance.setAvailabilityStatus(request.getAvailabilityStatus());
        }
        if (request.getCurrentLatitude() != null) {
            ambulance.setCurrentLatitude(request.getCurrentLatitude());
        }
        if (request.getCurrentLongitude() != null) {
            ambulance.setCurrentLongitude(request.getCurrentLongitude());
        }

        Ambulance updated = ambulanceRepository.save(ambulance);
        return mapper.toAmbulanceDTO(updated);
    }

    @Override
    @Transactional
    public void deleteAmbulance(Long id) {
        Ambulance ambulance = ambulanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ambulance not found with id: " + id));
        if (bookingRepository.existsByAmbulanceId(id)) {
            ambulance.setAvailabilityStatus(AmbulanceAvailability.OFFLINE);
            ambulance.setDriver(null);
            ambulanceRepository.save(ambulance);
            log.info("Ambulance {} has historical bookings. Successfully decommissioned to OFFLINE.", ambulance.getAmbulanceNumber());
        } else {
            ambulanceRepository.delete(ambulance);
        }
    }

    @Override
    public Ambulance findNearestAvailableAmbulance(double pickupLat, double pickupLon, AmbulanceType requestedType) {
        log.info("Finding nearest available ambulance for pickup [{}, {}], type: {}", pickupLat, pickupLon, requestedType);

        // 1. Try to find available ambulances matching requested type
        List<Ambulance> candidates = ambulanceRepository.findByAmbulanceTypeAndAvailabilityStatus(
                requestedType, AmbulanceAvailability.AVAILABLE
        );

        // 2. Fallback to any available ambulance if exact type is out of stock (life safety priority)
        if (candidates.isEmpty()) {
            log.warn("No available ambulances for type {}. Searching all available fleet units.", requestedType);
            candidates = ambulanceRepository.findByAvailabilityStatus(AmbulanceAvailability.AVAILABLE);
        }

        if (candidates.isEmpty()) {
            log.error("No ambulances available in entire fleet!");
            return null;
        }

        // 3. Find the one with minimum Haversine distance
        return candidates.stream()
                .min(Comparator.comparingDouble(a -> distanceUtil.calculateDistance(pickupLat, pickupLon, a.getCurrentLatitude(), a.getCurrentLongitude())))
                .orElse(null);
    }
}
