package com.melnikov.TicketBookingService.services;

import com.melnikov.TicketBookingService.dao.BookingDao;
import com.melnikov.TicketBookingService.dao.TicketDao;
import com.melnikov.TicketBookingService.dao.UserDao;
import com.melnikov.TicketBookingService.dto.BookingCreateRequestDto;
import com.melnikov.TicketBookingService.dto.BookingResponseDto;
import com.melnikov.TicketBookingService.entity.Ticket;
import com.melnikov.TicketBookingService.entity.Booking;
import com.melnikov.TicketBookingService.entity.User;
import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Log4j2
@Service
public class BookingService {

    private final BookingDao bookingDao;
    private final TicketDao ticketDao;
    private final UserDao userDao;

    public BookingService(BookingDao bookingDao, TicketDao ticketDao, UserDao userDao) {
        this.bookingDao = bookingDao;
        this.ticketDao = ticketDao;
        this.userDao = userDao;
    }

    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingCreateRequestDto request) {
        log.debug("Creating booking for user ID: {}, request: {}", userId, request);

        Ticket ticket = ticketDao.findByIdWithDetails(request.getTicketId())
                .orElseThrow(() -> {
                    log.error("Ticket not found with ID: {}", request.getTicketId());
                    return new IllegalArgumentException("Ticket not found");
                });

        log.debug("Found ticket: {}", ticket);

        if (ticket.getAvailableTickets() < request.getTicketQuantity()) {
            log.warn("Not enough tickets available. Requested: {}, Available: {}",
                    request.getTicketQuantity(), ticket.getAvailableTickets());
            throw new IllegalArgumentException("Not enough tickets available");
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new IllegalArgumentException("User not found");
                });

        log.debug("Found user: {}", user);

        Integer totalCost = ticket.getPrice() * request.getTicketQuantity();
        log.debug("Calculated total cost: {}", totalCost);

        if (user.getBalance() < totalCost) {
            log.warn("Insufficient balance. User balance: {}, Required: {}",
                    user.getBalance(), totalCost);
            throw new IllegalArgumentException("Insufficient balance");
        }

        boolean balanceUpdated = userDao.updateBalanceById(user.getId(), user.getBalance() - totalCost);
        log.debug("Balance updated: {}", balanceUpdated ? "success" : "failed");

        if (!balanceUpdated) {
            log.error("Balance update failed for user ID: {}", user.getId());
            throw new IllegalStateException("Failed to update user balance");
        }

        ticketDao.updateAvailableTickets(ticket.getId(), ticket.getAvailableTickets() - request.getTicketQuantity());
        log.debug("Updated available tickets for ticket ID: {}. New quantity: {}",
                ticket.getId(), ticket.getAvailableTickets() - request.getTicketQuantity());

        Booking booking = Booking.builder()
                .userId(userId)
                .ticketId(ticket.getId())
                .bookingTime(ZonedDateTime.now())
                .status("pending")
                .ticketQuantity(request.getTicketQuantity())
                .build();

        log.debug("Creating booking: {}", booking);
        booking = bookingDao.save(booking);
        log.info("Booking created successfully. ID: {}", booking.getId());

        return mapToResponseDto(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        log.debug("Fetching bookings for user ID: {}", userId);
        List<Booking> bookings = bookingDao.findAllByUserId(userId);
        log.debug("Found {} bookings for user ID: {}", bookings.size(), userId);
        return bookings;
    }

    @Transactional
    public void cancelBooking(Long userId, Integer bookingId) {
        log.debug("Canceling booking ID: {} for user ID: {}", bookingId, userId);

        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found with ID: {}", bookingId);
                    return new IllegalArgumentException("Booking not found");
                });

        log.debug("Found booking: {}", booking);

        if (!booking.getUserId().equals(userId)) {
            log.warn("User ID mismatch. Requested user: {}, Booking owner: {}",
                    userId, booking.getUserId());
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        if ("canceled".equals(booking.getStatus())) {
            log.warn("Booking already canceled. ID: {}", bookingId);
            throw new IllegalStateException("Booking is already canceled");
        }

        bookingDao.updateStatus(bookingId, "canceled");
        log.debug("Booking status updated to 'canceled'. ID: {}", bookingId);

        Ticket ticket = ticketDao.findByIdWithDetails(booking.getTicketId())
                .orElseThrow(() -> {
                    log.error("Ticket not found with ID: {}", booking.getTicketId());
                    return new IllegalArgumentException("Ticket not found");
                });

        int newQuantity = ticket.getAvailableTickets() + booking.getTicketQuantity();
        ticketDao.updateAvailableTickets(ticket.getId(), newQuantity);
        log.info("Returned {} tickets for ticket ID: {}. New available quantity: {}",
                booking.getTicketQuantity(), ticket.getId(), newQuantity);
    }

    private BookingResponseDto mapToResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .ticketId(booking.getTicketId())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .ticketQuantity(booking.getTicketQuantity())
                .build();
    }
}
