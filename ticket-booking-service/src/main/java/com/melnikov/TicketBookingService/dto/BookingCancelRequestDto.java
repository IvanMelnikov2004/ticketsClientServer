package com.melnikov.TicketBookingService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCancelRequestDto {
    @NotNull(message = "Booking ID is required")
    private Integer bookingId;
}