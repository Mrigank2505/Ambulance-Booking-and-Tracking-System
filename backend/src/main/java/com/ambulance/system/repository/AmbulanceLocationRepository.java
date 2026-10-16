package com.ambulance.system.repository;

import com.ambulance.system.entity.AmbulanceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmbulanceLocationRepository extends JpaRepository<AmbulanceLocation, Long> {
    Optional<AmbulanceLocation> findTop1ByAmbulanceIdOrderByTimestampDesc(Long ambulanceId);
    Optional<AmbulanceLocation> findTop1ByBookingIdOrderByTimestampDesc(Long bookingId);
    List<AmbulanceLocation> findByBookingIdOrderByTimestampAsc(Long bookingId);
}
