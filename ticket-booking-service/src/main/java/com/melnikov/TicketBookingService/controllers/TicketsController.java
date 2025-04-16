package com.melnikov.TicketBookingService.controllers;

import com.melnikov.TicketBookingService.dto.*;
import com.melnikov.TicketBookingService.entity.Ticket;
import com.melnikov.TicketBookingService.services.TicketService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/tickets")
public class TicketsController {
    private final TicketService ticketService;

    public TicketsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketCreateRequestDto request) {
        log.info("Server time: {}", ZonedDateTime.now());
        log.info("Received departureTime: {}", request.getDepartureTime());
        log.info("Received arrivalTime: {}", request.getArrivalTime());
        Ticket createdTicket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PostMapping("/search")
    public TicketSearchResponseDto searchTickets(@Valid @RequestBody TicketSearchRequestDto request) {

        return ticketService.searchTickets(request);
    }


}