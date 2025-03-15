package com.melnikov.TicketBookingService.controllers;

import com.melnikov.TicketBookingService.dto.BookingCancelRequestDto;
import com.melnikov.TicketBookingService.dto.BookingCreateRequestDto;
import com.melnikov.TicketBookingService.dto.BookingResponseDto;
import com.melnikov.TicketBookingService.entity.Booking;
import com.melnikov.TicketBookingService.services.BookingService;
import com.melnikov.TicketBookingService.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final JwtService jwtService;

    public BookingController(BookingService bookingService, JwtService jwtService) {
        this.bookingService = bookingService;
        this.jwtService = jwtService;
    }

    /**
     * Создание брони
     */
    @PostMapping("/create")
    public BookingResponseDto createBooking(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BookingCreateRequestDto request) {

        Long userId = extractUserIdFromToken(authHeader);
        return bookingService.createBooking(userId, request);
    }

    /**
     * Получение всех броней пользователя
     */
    @GetMapping("/list")
    public List<Booking> getUserBookings(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        return bookingService.getUserBookings(userId);
    }

    /**
     * Отмена брони
     */
    @PostMapping("/cancel")
    public void cancelBooking(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BookingCancelRequestDto request) {

        Long userId = extractUserIdFromToken(authHeader);
        bookingService.cancelBooking(userId, request.getBookingId());
    }

    private Long extractUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        return claims.get("id", Long.class);
    }
}
