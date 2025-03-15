package com.melnikov.TicketBookingService.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class TicketSearchRequestDto {
    @Pattern(regexp = "^(bus|avia|train)?$", message = "Invalid transport type")
    private String type;

    @NotBlank(message = "Departure place is required")
    private String from;

    @NotBlank(message = "Arrival place is required")
    private String to;

    @Future(message = "Start time must be in the future")
    private ZonedDateTime startTime;

    @Future(message = "End time must be in the future")
    private ZonedDateTime endTime;

    @Future(message = "Cursor must be in the future")
    private ZonedDateTime lastDepartureTime; // Курсор по времени отправления

    @PositiveOrZero(message = "Cursor ID must be zero or positive")
    private Integer lastId = 0; // Курсор по ID (уникальность)

    @Max(value = 15, message = "Page size must not exceed 15")
    @Min(value = 5, message = "Page size must be at least 5")
    private int pageSize = 10; // Размер страницы по умолчанию
}