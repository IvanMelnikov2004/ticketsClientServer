package com.melnikov.TicketBookingService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private Integer id;
    private String transportType;  // Название типа транспорта (bus/avia/plane)
    private String departureCity;  // Город отправления
    private String arrivalCity;    // Город прибытия
    private ZonedDateTime departureTime;
    private ZonedDateTime arrivalTime;
    private Integer price;
    private Integer availableTickets;

    // Служебные поля для связи с БД
    private Integer transportTypeId;
    private Integer routeId;
}