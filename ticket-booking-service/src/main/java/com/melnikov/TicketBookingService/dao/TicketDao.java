package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Ticket;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketDao {
    Ticket save(Ticket ticket);
    Optional<Ticket> findByIdWithDetails(Integer id);
    List<Ticket> findTickets(Integer transportTypeId, Integer routeId, ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime lastDepartureTime, int lastId, int pageSize);
    List<Ticket> findTicketsWithoutTransportType(Integer routeId, ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime lastDepartureTime, int lastId, int pageSize);
    List<Ticket> findClosestTicketsWithPagination(String departureCity, String arrivalCity, ZonedDateTime lastDepartureTime, Integer lastId, int pageSize);
    void updateAvailableTickets(Integer ticketId, int newAvailableTickets);

}
