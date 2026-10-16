package com.ambulance.system.mapper;

import com.ambulance.system.dto.*;
import com.ambulance.system.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityDtoMapper {

    public UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    public DriverDTO toDriverDTO(Driver driver) {
        if (driver == null) return null;
        String assignedAmbulanceNumber = null;
        return new DriverDTO(
                driver.getId(),
                driver.getUser() != null ? driver.getUser().getId() : null,
                driver.getUser() != null ? driver.getUser().getFullName() : null,
                driver.getUser() != null ? driver.getUser().getEmail() : null,
                driver.getUser() != null ? driver.getUser().getPhone() : null,
                driver.getLicenseNumber(),
                driver.getAvailabilityStatus(),
                driver.getCurrentLatitude(),
                driver.getCurrentLongitude(),
                assignedAmbulanceNumber
        );
    }

    public AmbulanceDTO toAmbulanceDTO(Ambulance ambulance) {
        if (ambulance == null) return null;
        Long driverId = ambulance.getDriver() != null ? ambulance.getDriver().getId() : null;
        String driverName = null;
        String driverPhone = null;
        if (ambulance.getDriver() != null && ambulance.getDriver().getUser() != null) {
            driverName = ambulance.getDriver().getUser().getFullName();
            driverPhone = ambulance.getDriver().getUser().getPhone();
        }

        return new AmbulanceDTO(
                ambulance.getId(),
                ambulance.getAmbulanceNumber(),
                ambulance.getAmbulanceType(),
                driverId,
                driverName,
                driverPhone,
                ambulance.getCapacity(),
                ambulance.getEquipmentDetails(),
                ambulance.getAvailabilityStatus(),
                ambulance.getCurrentLatitude(),
                ambulance.getCurrentLongitude(),
                null,
                null
        );
    }

    public HospitalDTO toHospitalDTO(Hospital hospital) {
        if (hospital == null) return null;
        return new HospitalDTO(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhone(),
                hospital.getLatitude(),
                hospital.getLongitude(),
                hospital.getEmergencyAvailable(),
                hospital.getIcuAvailable(),
                hospital.getDescription(),
                null
        );
    }

    public BookingDTO toBookingDTO(Booking booking) {
        if (booking == null) return null;
        Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
        String userName = booking.getUser() != null ? booking.getUser().getFullName() : null;

        Long ambulanceId = booking.getAmbulance() != null ? booking.getAmbulance().getId() : null;
        String ambulanceNumber = booking.getAmbulance() != null ? booking.getAmbulance().getAmbulanceNumber() : null;

        Long driverId = booking.getDriver() != null ? booking.getDriver().getId() : null;
        String driverName = null;
        String driverPhone = null;
        if (booking.getDriver() != null && booking.getDriver().getUser() != null) {
            driverName = booking.getDriver().getUser().getFullName();
            driverPhone = booking.getDriver().getUser().getPhone();
        }

        Long hospitalId = booking.getHospital() != null ? booking.getHospital().getId() : null;
        String hospitalName = booking.getHospital() != null ? booking.getHospital().getName() : null;

        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setBookingCode(booking.getBookingCode());
        dto.setUserId(userId);
        dto.setUserName(userName);
        dto.setAmbulanceId(ambulanceId);
        dto.setAmbulanceNumber(ambulanceNumber);
        dto.setDriverId(driverId);
        dto.setDriverName(driverName);
        dto.setDriverPhone(driverPhone);
        dto.setHospitalId(hospitalId);
        dto.setHospitalName(hospitalName);
        dto.setPatientName(booking.getPatientName());
        dto.setPatientPhone(booking.getPatientPhone());
        dto.setPickupAddress(booking.getPickupAddress());
        dto.setPickupLatitude(booking.getPickupLatitude());
        dto.setPickupLongitude(booking.getPickupLongitude());
        dto.setDestinationAddress(booking.getDestinationAddress());
        dto.setDestinationLatitude(booking.getDestinationLatitude());
        dto.setDestinationLongitude(booking.getDestinationLongitude());
        dto.setEmergencyType(booking.getEmergencyType());
        dto.setAmbulanceType(booking.getAmbulanceType());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setNotes(booking.getNotes());
        dto.setEstimatedArrivalTime(booking.getEstimatedArrivalTime());
        dto.setDistanceKm(booking.getDistanceKm());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }

    public NotificationDTO toNotificationDTO(Notification notification) {
        if (notification == null) return null;
        return new NotificationDTO(
                notification.getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getBooking() != null ? notification.getBooking().getId() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }

    public EmergencyContactDTO toEmergencyContactDTO(EmergencyContact contact) {
        if (contact == null) return null;
        return new EmergencyContactDTO(
                contact.getId(),
                contact.getUser() != null ? contact.getUser().getId() : null,
                contact.getName(),
                contact.getPhone(),
                contact.getRelationship()
        );
    }
}
