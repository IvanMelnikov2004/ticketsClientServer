package com.melnikov.TicketBookingService.dto;

import com.melnikov.TicketBookingService.validation.ArrivalAfterDeparture;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@ArrivalAfterDeparture
public class TicketCreateRequestDto {
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(bus|avia|train)$", message = "Invalid transport type")
    private String type;

    @NotBlank(message = "Departure city is required")
    private String from;

    @NotBlank(message = "Arrival city is required")
    private String to;

    @NotNull
    @Future(message = "Departure time must be in the future")
    private ZonedDateTime departureTime;

    @NotNull
    @Future(message = "Arrival time must be in the future")
    private ZonedDateTime arrivalTime;


    @Positive(message = "Price must be positive")
    private Integer price;

    @Min(value = 0, message = "Available tickets cannot be negative")
    private Integer availableTickets;
}