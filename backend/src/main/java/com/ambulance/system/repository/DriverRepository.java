package com.ambulance.system.repository;

import com.ambulance.system.entity.Driver;
import com.ambulance.system.entity.DriverAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserId(Long userId);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    Optional<Driver> findByLicenseNumberIgnoreCase(String licenseNumber);
    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);
    boolean existsByLicenseNumber(String licenseNumber);
    List<Driver> findByAvailabilityStatus(DriverAvailability availabilityStatus);
    long countByAvailabilityStatus(DriverAvailability availabilityStatus);
}
