package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Booking;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingDaoImpl implements BookingDao{

    private final JdbcTemplate jdbcTemplate;

    public BookingDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Booking> bookingRowMapper = (rs, rowNum) -> Booking.builder()
            .id(rs.getInt("id"))
            .userId(rs.getLong("user_id"))
            .ticketId(rs.getInt("ticket_id"))
            .bookingTime(rs.getTimestamp("booking_time").toInstant().atZone(java.time.ZoneId.systemDefault()))
            .status(rs.getString("status"))
            .ticketQuantity(rs.getInt("ticket_quantity"))
            .build();

    public Booking save(Booking booking) {
        String sql = """
            INSERT INTO bookings (user_id, ticket_id, booking_time, status, ticket_quantity)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id;
        """;

        int id = jdbcTemplate.queryForObject(sql, Integer.class,
                booking.getUserId(),
                booking.getTicketId(),
                Timestamp.from(booking.getBookingTime().toInstant()),
                booking.getStatus(),
                booking.getTicketQuantity());

        booking.setId(id);
        return booking;
    }

    public List<Booking> findAllByUserId(Long userId) {
        String sql = """
            SELECT * FROM bookings
            WHERE user_id = ?
            ORDER BY booking_time DESC;
        """;
        return jdbcTemplate.query(sql, bookingRowMapper, userId);
    }

    public Optional<Booking> findById(Integer bookingId) {
        String sql = """
            SELECT * FROM bookings
            WHERE id = ?;
        """;
        return jdbcTemplate.query(sql, bookingRowMapper, bookingId).stream().findFirst();
    }

    public void updateStatus(Integer bookingId, String status) {
        String sql = """
            UPDATE bookings
            SET status = ?
            WHERE id = ?;
        """;
        jdbcTemplate.update(sql, status, bookingId);
    }
}