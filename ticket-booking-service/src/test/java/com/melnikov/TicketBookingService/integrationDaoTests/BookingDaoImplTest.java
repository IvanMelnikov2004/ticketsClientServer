package com.melnikov.TicketBookingService.integrationDaoTests;

import com.melnikov.TicketBookingService.dao.BookingDaoImpl;
import com.melnikov.TicketBookingService.entity.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class BookingDaoImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BookingDaoImpl bookingDao;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        initializeSchema();
        insertTestData();
    }

    private void cleanDatabase() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS bookings CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS users CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tickets CASCADE");
    }

    private void initializeSchema() {
        jdbcTemplate.execute("CREATE TABLE users (" +
                "id SERIAL PRIMARY KEY, " +
                "email VARCHAR(255) NOT NULL UNIQUE)");

        jdbcTemplate.execute("CREATE TABLE tickets (" +
                "id SERIAL PRIMARY KEY, " +
                "available_tickets INTEGER NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE bookings (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL REFERENCES users(id), " +
                "ticket_id INTEGER NOT NULL REFERENCES tickets(id), " +
                "booking_time TIMESTAMP NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "ticket_quantity INTEGER NOT NULL)");
    }

    private void insertTestData() {
        jdbcTemplate.update("INSERT INTO users(id, email) VALUES (1, 'user@test.com')");
        jdbcTemplate.update("INSERT INTO tickets(id, available_tickets) VALUES (1, 100), (2, 50)");
    }

    private Booking createTestBooking() {
        return Booking.builder()
                .userId(1L)
                .ticketId(1)
                .bookingTime(ZonedDateTime.now())
                .status("PENDING")
                .ticketQuantity(2)
                .build();
    }

    @Test
    void shouldSaveAndRetrieveBooking() {
        // Given
        Booking booking = createTestBooking();

        // When
        Booking saved = bookingDao.save(booking);
        Optional<Booking> found = bookingDao.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        Booking dbBooking = found.get();
        assertAll(
                () -> assertEquals(1L, dbBooking.getUserId()),
                () -> assertEquals(1, dbBooking.getTicketId()),
                () -> assertEquals("PENDING", dbBooking.getStatus()),
                () -> assertEquals(2, dbBooking.getTicketQuantity()),
                () -> assertNotNull(dbBooking.getBookingTime())
        );
    }

    @Test
    void shouldFindAllUserBookingsOrderedByTime() {
        // Create bookings with different times
        bookingDao.save(createBookingWithTime("2024-01-01T12:00:00"));
        bookingDao.save(createBookingWithTime("2024-01-01T14:00:00"));
        bookingDao.save(createBookingWithTime("2024-01-01T10:00:00"));

        List<Booking> bookings = bookingDao.findAllByUserId(1L);

        assertEquals(3, bookings.size());
        // Verify descending order
        assertTrue(bookings.get(0).getBookingTime().isAfter(bookings.get(1).getBookingTime()));
        assertTrue(bookings.get(1).getBookingTime().isAfter(bookings.get(2).getBookingTime()));
    }

    @Test
    void shouldUpdateBookingStatus() {
        Booking booking = bookingDao.save(createTestBooking());

        bookingDao.updateStatus(booking.getId(), "CONFIRMED");

        Optional<Booking> updated = bookingDao.findById(booking.getId());
        assertTrue(updated.isPresent());
        assertEquals("CONFIRMED", updated.get().getStatus());
    }

    @Test
    void shouldHandleForeignKeysConstraints() {
        // Non-existing user
        Booking booking = createTestBooking();
        booking.setUserId(999L);
        assertThrows(DataIntegrityViolationException.class, () -> bookingDao.save(booking));

        // Non-existing ticket
        Booking booking2 = createTestBooking();
        booking2.setTicketId(999);
        assertThrows(DataIntegrityViolationException.class, () -> bookingDao.save(booking2));
    }

    @Test
    void shouldReturnEmptyForNonExistingBooking() {
        Optional<Booking> found = bookingDao.findById(999);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldHandleMultipleStatusUpdates() {
        Booking booking = bookingDao.save(createTestBooking());

        bookingDao.updateStatus(booking.getId(), "PROCESSING");
        bookingDao.updateStatus(booking.getId(), "COMPLETED");
        bookingDao.updateStatus(booking.getId(), "CANCELLED");

        Optional<Booking> updated = bookingDao.findById(booking.getId());
        assertTrue(updated.isPresent());
        assertEquals("CANCELLED", updated.get().getStatus());
    }



    @Test
    void shouldHandleMaxTicketQuantity() {
        Booking booking = createTestBooking();
        booking.setTicketQuantity(1000);

        Booking saved = bookingDao.save(booking);
        Optional<Booking> found = bookingDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(1000, found.get().getTicketQuantity());
    }

    @Test
    void shouldReturnEmptyListForUserWithoutBookings() {
        List<Booking> bookings = bookingDao.findAllByUserId(1L);
        assertTrue(bookings.isEmpty());
    }

    private Booking createBookingWithTime(String time) {
        ZonedDateTime bookingTime = ZonedDateTime.parse(time + "Z");
        return Booking.builder()
                .userId(1L)
                .ticketId(1)
                .bookingTime(bookingTime)
                .status("PENDING")
                .ticketQuantity(1)
                .build();
    }
}