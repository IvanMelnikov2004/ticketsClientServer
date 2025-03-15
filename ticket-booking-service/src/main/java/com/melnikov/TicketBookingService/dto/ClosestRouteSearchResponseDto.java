package com.melnikov.TicketBookingService.dto;

import com.melnikov.TicketBookingService.entity.Ticket;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class ClosestRouteSearchResponseDto {

    private List<Ticket> tickets; // Список найденных билетов

    private ZonedDateTime nextCursorDepartureTime; // Время отправления последнего билета (для пагинации)

    private Integer nextCursorId; // ID последнего билета (для пагинации)

}