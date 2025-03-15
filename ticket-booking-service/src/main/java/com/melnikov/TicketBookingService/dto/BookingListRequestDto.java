package com.melnikov.TicketBookingService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingListRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;
}