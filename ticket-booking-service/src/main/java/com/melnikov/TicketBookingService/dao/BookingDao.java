package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingDao {
    Booking save(Booking booking);
    List<Booking> findAllByUserId(Long userId);
    Optional<Booking> findById(Integer bookingId);
    void updateStatus(Integer bookingId, String status);
}
