package com.melnikov.TicketBookingService.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ClosestRouteSearchRequestDto {

    @NotBlank(message = "Departure city is required")
    private String from; // Город отправления

    @NotBlank(message = "Arrival city is required")
    private String to; // Город прибытия

    @NotNull(message = "Desired departure time is required")
    @Future(message = "Departure time must be in the future")
    private ZonedDateTime desiredDepartureTime; // Желаемое время отправления

    @Min(value = 1, message = "Page size must be at least 1")
    private int pageSize = 5; // Количество результатов (по умолчанию 5)

    private Integer lastId; // ID последнего билета (для курсора)

    private ZonedDateTime lastDepartureTime; // Время отправления последнего билета (для курсора)
}