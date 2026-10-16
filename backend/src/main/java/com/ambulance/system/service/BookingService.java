package com.ambulance.system.service;

import com.ambulance.system.dto.BookingDTO;
import com.ambulance.system.dto.BookingRequest;
import com.ambulance.system.dto.BookingStatusUpdateRequest;
import com.ambulance.system.entity.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingRequest request);
    BookingDTO getBookingById(Long id);
    BookingDTO getBookingByCode(String bookingCode);
    List<BookingDTO> getCurrentUserBookings();
    List<BookingDTO> getBookingsByUserId(Long userId);
    List<BookingDTO> getAllBookings();
    BookingDTO cancelBooking(Long id, String reason);
    BookingDTO driverAcceptBooking(Long id);
    BookingDTO driverRejectBooking(Long id, String reason);
    BookingDTO updateBookingStatus(Long id, BookingStatusUpdateRequest request);
    BookingDTO driverMarkArrived(Long id);
    BookingDTO driverMarkPickedUp(Long id);
    BookingDTO driverMarkCompleted(Long id);
}
