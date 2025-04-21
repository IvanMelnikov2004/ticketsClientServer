package com.melnikov.TicketBookingService.services;

import com.melnikov.TicketBookingService.dao.RouteDao;
import com.melnikov.TicketBookingService.dao.TicketDao;
import com.melnikov.TicketBookingService.dto.*;
import com.melnikov.TicketBookingService.entity.Ticket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
@Slf4j
@Service
public class TicketService {
    private final TicketDao ticketDao;
    private final RouteDao routeDao;

    public TicketService(TicketDao ticketDao, RouteDao routeDao) {
        this.ticketDao = ticketDao;
        this.routeDao = routeDao;
    }

    @Transactional
    public Ticket createTicket(TicketCreateRequestDto request) {
        // Получаем или создаем маршрут
        Integer routeId = routeDao.findIdByCities(request.getFrom(), request.getTo())
                .orElseGet(() -> {
                    routeDao.createRoute(request.getFrom(), request.getTo());
                    return routeDao.findIdByCities(request.getFrom(), request.getTo())
                            .orElseThrow(() -> new RuntimeException("Route creation failed"));
                });

        // Создаем объект билета
        Ticket ticket = new Ticket();
        int idType = switch (request.getType()) {
            case "bus" -> 1;
            case "avia" -> 2;
            case "train" -> 3;
            default -> 0;
        };
        ticket.setTransportTypeId(idType); // ID типа транспорта передается в запросе
        ticket.setRouteId(routeId);
        ticket.setDepartureTime(request.getDepartureTime());
        ticket.setArrivalTime(request.getArrivalTime());
        ticket.setPrice(request.getPrice());
        ticket.setAvailableTickets(request.getAvailableTickets());

        return ticketDao.save(ticket);
    }

    public Ticket getTicketDetails(Integer id) {
        return ticketDao.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    @Transactional()
    public TicketSearchResponseDto searchTickets(TicketSearchRequestDto request) {
        ZonedDateTime defaultStartTime = ZonedDateTime.now().minusYears(10);
        ZonedDateTime defaultEndTime = ZonedDateTime.now().plusYears(10);
        log.debug("recieved :" + request);
        // Устанавливаем курсор и размер страницы
        ZonedDateTime lastDepartureTime = request.getLastDepartureTime() != null ? request.getLastDepartureTime() : ZonedDateTime.now().minusYears(10);
        int lastId = request.getLastId() != null ? request.getLastId() : 0; // Если lastId не передан, берем 0
        int pageSize = request.getPageSize();

        // Ищем маршрут
        Integer routeId = routeDao.findIdByCities(request.getFrom(), request.getTo())
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        // Задаем временные границы поиска
        ZonedDateTime startTime = request.getStartTime() != null ? request.getStartTime() : defaultStartTime;
        ZonedDateTime endTime = request.getEndTime() != null ? request.getEndTime() : defaultEndTime;

        // Создаем объект ответа
        TicketSearchResponseDto response = new TicketSearchResponseDto();
        List<Ticket> ticketList;

        // Ищем билеты с или без фильтра по транспорту
        if (request.getType() != null) {
            ticketList = ticketDao.findTickets(
                    mapTransportType(request.getType()),
                    routeId,
                    startTime,
                    endTime,
                    lastDepartureTime, // Передаем курсор по времени
                    lastId, // Передаем курсор по ID
                    pageSize // Передаем размер страницы
            );
        } else {
            ticketList = ticketDao.findTicketsWithoutTransportType(
                    routeId,
                    startTime,
                    endTime,
                    lastDepartureTime, // Передаем курсор по времени
                    lastId, // Передаем курсор по ID
                    pageSize // Передаем размер страницы
            );
        }

        // Устанавливаем билеты в ответ
        response.setTickets(ticketList);
        if (!ticketList.isEmpty()) {
            Ticket lastTicket = ticketList.get(ticketList.size() - 1);
            response.setNextCursor(lastTicket.getDepartureTime());
            response.setNextId(lastTicket.getId());
        }
        for (Ticket ticket : ticketList){
            ticket.setArrivalCity(request.getTo());
            ticket.setDepartureCity(request.getFrom());
            ticket.setDepartureTime(ticket.getDepartureTime().minusHours(3));
            ticket.setArrivalTime(ticket.getArrivalTime().minusHours(3));
            String transportType = switch (ticket.getTransportTypeId()) {
                case 1 -> "bus";
                case 2 -> "avia";
                case 3 -> "train";
                default -> "null";
            };
            ticket.setTransportType(transportType);
        }
        // Устанавливаем новый курсор, если есть данные


        return response;
    }




    private int mapTransportType(String type) {
        return switch (type) {
            case "bus" -> 1;
            case "avia" -> 2;
            case "train" -> 3;
            default -> throw new IllegalArgumentException("Invalid transport type: " + type);
        };
    }
}