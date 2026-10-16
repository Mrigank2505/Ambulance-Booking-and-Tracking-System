package com.ambulance.system.repository;

import com.ambulance.system.entity.Booking;
import com.ambulance.system.entity.BookingStatus;
import com.ambulance.system.entity.EmergencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Booking> findByDriverIdAndBookingStatusInOrderByCreatedAtDesc(Long driverId, Collection<BookingStatus> statuses);
    List<Booking> findByAmbulanceIdOrderByCreatedAtDesc(Long ambulanceId);
    List<Booking> findByBookingStatusInOrderByCreatedAtDesc(Collection<BookingStatus> statuses);
    boolean existsByAmbulanceId(Long ambulanceId);
    boolean existsByHospitalId(Long hospitalId);
    long countByBookingStatus(BookingStatus bookingStatus);
    long countByEmergencyType(EmergencyType emergencyType);
}
