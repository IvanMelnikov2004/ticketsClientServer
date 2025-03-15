package com.melnikov.TicketBookingService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCreateRequestDto {

    @NotNull(message = "Ticket ID is required")
    private Integer ticketId;

    @Min(value = 1, message = "Ticket quantity must be at least 1")
    private Integer ticketQuantity;
}