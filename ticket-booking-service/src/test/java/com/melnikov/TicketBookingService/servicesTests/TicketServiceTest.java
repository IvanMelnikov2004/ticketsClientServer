package com.melnikov.TicketBookingService.servicesTests;



import com.melnikov.TicketBookingService.dao.RouteDao;
import com.melnikov.TicketBookingService.dao.TicketDao;
import com.melnikov.TicketBookingService.dto.TicketCreateRequestDto;
import com.melnikov.TicketBookingService.dto.TicketSearchRequestDto;
import com.melnikov.TicketBookingService.dto.TicketSearchResponseDto;
import com.melnikov.TicketBookingService.entity.Ticket;
import com.melnikov.TicketBookingService.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private RouteDao routeDao;

    @Mock
    private TicketDao ticketDao;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {

    }

    @Test
    void createTicket_existingRoute_savesTicket() {

        TicketCreateRequestDto request = new TicketCreateRequestDto();
        request.setType("bus");
        request.setFrom("CityA");
        request.setTo("CityB");
        request.setDepartureTime(ZonedDateTime.now().plusDays(1));
        request.setArrivalTime(ZonedDateTime.now().plusDays(1).plusHours(2));
        request.setPrice(100);
        request.setAvailableTickets(10);

        when(routeDao.findIdByCities("CityA", "CityB")).thenReturn(Optional.of(42));
        Ticket saved = new Ticket(); saved.setId(1);
        when(ticketDao.save(any(Ticket.class))).thenReturn(saved);

        // Act
        Ticket result = ticketService.createTicket(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDao).save(captor.capture());
        Ticket toSave = captor.getValue();
        assertEquals(42, toSave.getRouteId());
        assertEquals(1, toSave.getTransportTypeId()); // bus -> 1
    }

    @Test
    void createTicket_newRoute_createsAndSaves() {
        TicketCreateRequestDto request = new TicketCreateRequestDto();
        request.setType("train");
        request.setFrom("A");
        request.setTo("B");
        request.setDepartureTime(ZonedDateTime.now().plusDays(2));
        request.setArrivalTime(ZonedDateTime.now().plusDays(2).plusHours(3));
        request.setPrice(200);
        request.setAvailableTickets(5);

        when(routeDao.findIdByCities("A", "B")).thenReturn(Optional.empty())
                .thenReturn(Optional.of(99));

        Ticket saved = new Ticket(); saved.setId(2);
        when(ticketDao.save(any(Ticket.class))).thenReturn(saved);

        Ticket result = ticketService.createTicket(request);

        assertEquals(2, result.getId());
        verify(routeDao).createRoute("A", "B");
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDao).save(captor.capture());
        Ticket toSave = captor.getValue();
        assertEquals(99, toSave.getRouteId());
        assertEquals(3, toSave.getTransportTypeId()); // train -> 3
    }

    @Test
    void getTicketDetails_found_returnsTicket() {
        Ticket ticket = new Ticket(); ticket.setId(5);
        when(ticketDao.findByIdWithDetails(5)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketDetails(5);

        assertThat(result.getId()).isEqualTo(5);
    }

    @Test
    void getTicketDetails_notFound_throwsException() {
        when(ticketDao.findByIdWithDetails(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ticketService.getTicketDetails(1));
    }

    @Test
    void searchTickets_withType_filtersByTransportType() {
        // Arrange
        TicketSearchRequestDto request = new TicketSearchRequestDto();
        request.setFrom("X");
        request.setTo("Y");
        request.setType("avia");
        request.setPageSize(5);

        when(routeDao.findIdByCities("X", "Y")).thenReturn(Optional.of(7));
        Ticket t1 = Ticket.builder()
                .id(10)
                .transportTypeId(2)
                .departureTime(ZonedDateTime.now().plusDays(1).plusHours(3))
                .arrivalTime(ZonedDateTime.now().plusDays(1).plusHours(5))
                .build();
        when(ticketDao.findTickets(anyInt(), eq(7), any(), any(), any(), anyInt(), eq(5)))
                .thenReturn(List.of(t1));

        TicketSearchResponseDto response = ticketService.searchTickets(request);

        assertThat(response.getTickets()).hasSize(1);
        Ticket returned = response.getTickets().get(0);
        assertEquals("X", returned.getDepartureCity());
        assertEquals("Y", returned.getArrivalCity());
        assertEquals("avia", returned.getTransportType());
        assertNotNull(response.getNextCursor());
        assertEquals(10, response.getNextId());
    }

    @Test
    void searchTickets_withoutType_usesNoTransportFilter() {
        TicketSearchRequestDto request = new TicketSearchRequestDto();
        request.setFrom("M");
        request.setTo("N");
        request.setPageSize(6);

        when(routeDao.findIdByCities("M", "N")).thenReturn(Optional.of(8));
        when(ticketDao.findTicketsWithoutTransportType(eq(8), any(), any(), any(), anyInt(), eq(6)))
                .thenReturn(Collections.emptyList());

        TicketSearchResponseDto response = ticketService.searchTickets(request);

        assertTrue(response.getTickets().isEmpty());
        assertNull(response.getNextCursor());
        assertNull(response.getNextId());
    }
}
