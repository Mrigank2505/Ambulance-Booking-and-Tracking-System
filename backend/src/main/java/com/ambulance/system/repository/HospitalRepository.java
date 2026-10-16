package com.ambulance.system.repository;

import com.ambulance.system.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    List<Hospital> findByNameContainingIgnoreCase(String name);
    List<Hospital> findByEmergencyAvailableTrue();
    boolean existsByName(String name);
}
