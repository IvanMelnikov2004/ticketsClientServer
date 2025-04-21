package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Ticket;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Repository
public class TicketDaoImpl implements TicketDao {
    private final JdbcTemplate jdbcTemplate;

    public TicketDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Ticket> ticketRowMapper = (rs, rowNum) -> {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setTransportTypeId(rs.getInt("transport_type_id"));
        ticket.setRouteId(rs.getInt("route_id"));

        // Читаем Timestamp и конвертируем в ZonedDateTime с учетом часового пояса
        Timestamp departureTimestamp = rs.getTimestamp("departure_time");
        Timestamp arrivalTimestamp = rs.getTimestamp("arrival_time");

        // Установим системный часовой пояс по умолчанию или конкретный
        ticket.setDepartureTime(departureTimestamp.toInstant().atZone(java.time.ZoneId.of("UTC"))); // Пример: UTC
        ticket.setArrivalTime(arrivalTimestamp.toInstant().atZone(java.time.ZoneId.of("UTC"))); // Пример: UTC

        ticket.setPrice(rs.getInt("price"));
        ticket.setAvailableTickets(rs.getInt("available_tickets"));
        return ticket;
    };

    @Override
    public Ticket save(Ticket ticket) {
        String sql = """
            INSERT INTO tickets (transport_type_id, route_id, departure_time, 
                                arrival_time, price, available_tickets)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, ticket.getTransportTypeId());
            ps.setInt(2, ticket.getRouteId());
            ps.setTimestamp(3, Timestamp.from(ticket.getDepartureTime().toInstant()));
            ps.setTimestamp(4, Timestamp.from(ticket.getArrivalTime().toInstant()));
            ps.setInt(5, ticket.getPrice());
            ps.setInt(6, ticket.getAvailableTickets());
            return ps;
        }, keyHolder);

        ticket.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return ticket;
    }

    public Optional<Ticket> findByIdWithDetails(Integer id) {
        String sql = """
            SELECT t.*, tt.name as transport_type, 
                   r.departure_city, r.arrival_city
            FROM tickets t
            JOIN transport_types tt ON t.transport_type_id = tt.id
            JOIN routes r ON t.route_id = r.id
            WHERE t.id = ?
        """;

        try {
            Ticket ticket = jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                Ticket t = ticketRowMapper.mapRow(rs, rowNum);
                t.setTransportType(rs.getString("transport_type"));
                t.setDepartureCity(rs.getString("departure_city"));
                t.setArrivalCity(rs.getString("arrival_city"));
                return t;
            });
            return Optional.of(ticket);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateAvailableTickets(Integer ticketId, int newAvailableTickets) {
        String sql = """
        UPDATE tickets
        SET available_tickets = ?
        WHERE id = ?;
    """;

        jdbcTemplate.update(sql, newAvailableTickets, ticketId);
    }

    @Override
    public List<Ticket> findTickets(Integer transportTypeId, Integer routeId, ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime lastDepartureTime, int lastId, int pageSize) {
        String sql = """
            SELECT t.id, 
                   t.transport_type_id,
                   t.route_id,
                   t.departure_time,
                   t.arrival_time,
                   t.price,
                   t.available_tickets
            FROM tickets t
            JOIN transport_types tt ON t.transport_type_id = tt.id
            WHERE t.transport_type_id = ?
              AND t.available_tickets > 0
              AND t.route_id = ?
              AND t.departure_time >= ?
              AND t.departure_time <= ?
              AND (t.departure_time > ? OR (t.departure_time = ? AND t.id > ?)) -- Уникальный курсор
            ORDER BY t.departure_time, t.id
            LIMIT ?;
        """;

        return jdbcTemplate.query(sql, ticketRowMapper,
                transportTypeId,
                routeId,
                Timestamp.from(startTime.toInstant()),
                Timestamp.from(endTime.toInstant()),
                Timestamp.from(lastDepartureTime.toInstant()), // Курсор по времени
                Timestamp.from(lastDepartureTime.toInstant()), // Повторное сравнение по времени
                lastId, // Курсор по ID
                pageSize // Размер страницы
        );
    }

    @Override
    public List<Ticket> findTicketsWithoutTransportType(Integer routeId, ZonedDateTime startTime, ZonedDateTime endTime, ZonedDateTime lastDepartureTime, int lastId, int pageSize) {
        String sql = """
            SELECT t.id, 
                   t.transport_type_id,
                   t.route_id,
                   t.departure_time,
                   t.arrival_time,
                   t.price,
                   t.available_tickets
            FROM tickets t
            WHERE t.route_id = ?
              AND t.departure_time >= ?
              AND t.departure_time <= ?
              AND (t.departure_time > ? OR (t.departure_time = ? AND t.id > ?)) -- Уникальный курсор
            ORDER BY t.departure_time, t.id
            LIMIT ?;
        """;

        return jdbcTemplate.query(sql, ticketRowMapper,
                routeId,
                Timestamp.from(startTime.toInstant()),
                Timestamp.from(endTime.toInstant()),
                Timestamp.from(lastDepartureTime.toInstant()), // Курсор по времени
                Timestamp.from(lastDepartureTime.toInstant()), // Повторное сравнение по времени
                lastId, // Курсор по ID
                pageSize // Размер страницы
        );
    }
}