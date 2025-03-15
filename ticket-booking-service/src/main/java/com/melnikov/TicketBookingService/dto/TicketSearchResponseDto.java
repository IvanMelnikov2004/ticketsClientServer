package com.melnikov.TicketBookingService.dto;

import com.melnikov.TicketBookingService.entity.Ticket;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class TicketSearchResponseDto {
    private List<Ticket> tickets; // Список билетов в текущей порции
    private ZonedDateTime nextCursor; // Время отправления последнего билета в текущей порции (для курсора)
    private Integer nextId; // ID последнего билета в текущей порции (для уникального курсора)
}