package com.ambulance.system.repository;

import com.ambulance.system.entity.Ambulance;
import com.ambulance.system.entity.AmbulanceAvailability;
import com.ambulance.system.entity.AmbulanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    Optional<Ambulance> findByAmbulanceNumber(String ambulanceNumber);
    Optional<Ambulance> findByDriverId(Long driverId);
    List<Ambulance> findByAvailabilityStatus(AmbulanceAvailability availabilityStatus);
    List<Ambulance> findByAmbulanceTypeAndAvailabilityStatus(AmbulanceType ambulanceType, AmbulanceAvailability availabilityStatus);
    long countByAvailabilityStatus(AmbulanceAvailability availabilityStatus);
}
