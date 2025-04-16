package com.melnikov.TicketBookingService.integrationDaoTests;
import com.melnikov.TicketBookingService.entity.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.melnikov.TicketBookingService.dao.TicketDaoImpl;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class TicketDaoImplTest {

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
    private TicketDaoImpl ticketDao;

    private final ZoneId utc = ZoneId.of("UTC");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @BeforeEach
    void setUp() {
        cleanDatabase();
        initializeSchema();
        insertTestData();
    }

    private void cleanDatabase() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS tickets CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS transport_types CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS routes CASCADE");
    }

    private void initializeSchema() {
        jdbcTemplate.execute("CREATE TABLE transport_types (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL UNIQUE)");

        jdbcTemplate.execute("CREATE TABLE routes (" +
                "id SERIAL PRIMARY KEY, " +
                "departure_city VARCHAR(255) NOT NULL, " +
                "arrival_city VARCHAR(255) NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE tickets (" +
                "id SERIAL PRIMARY KEY, " +
                "transport_type_id INTEGER NOT NULL REFERENCES transport_types(id), " +
                "route_id INTEGER NOT NULL REFERENCES routes(id), " +
                "departure_time TIMESTAMP NOT NULL, " +
                "arrival_time TIMESTAMP NOT NULL, " +
                "price INTEGER NOT NULL, " +
                "available_tickets INTEGER NOT NULL)");
    }

    private void insertTestData() {
        jdbcTemplate.update("INSERT INTO transport_types(id, name) VALUES (1, 'Train'), (2, 'Bus')");
        jdbcTemplate.update("INSERT INTO routes(id, departure_city, arrival_city) VALUES " +
                "(1, 'Moscow', 'Saint Petersburg'), " +
                "(2, 'London', 'Paris')");
    }

    private Ticket createTestTicket(ZonedDateTime departure, ZonedDateTime arrival, int available) {
        Ticket ticket = new Ticket();
        ticket.setTransportTypeId(1);
        ticket.setRouteId(1);
        ticket.setDepartureTime(departure.withZoneSameInstant(utc));
        ticket.setArrivalTime(arrival.withZoneSameInstant(utc));
        ticket.setPrice(100);
        ticket.setAvailableTickets(available);
        return ticket;
    }

    @Test
    void shouldSaveAndRetrieveTicketWithUTCtimeZone() {
        ZonedDateTime departure = ZonedDateTime.of(2024, 1, 1, 10, 0, 0, 0, utc);
        ZonedDateTime arrival = departure.plusHours(4);

        Ticket saved = ticketDao.save(createTestTicket(departure, arrival, 50));
        Optional<Ticket> found = ticketDao.findByIdWithDetails(saved.getId());

        assertTrue(found.isPresent());
        Ticket dbTicket = found.get();

        assertAll(
                () -> assertEquals("Train", dbTicket.getTransportType()),
                () -> assertEquals("Moscow", dbTicket.getDepartureCity()),
                () -> assertEquals("Saint Petersburg", dbTicket.getArrivalCity()),
                () -> assertEquals(departure.toInstant(), dbTicket.getDepartureTime().toInstant()),
                () -> assertEquals("2024-01-01T10:00", dbTicket.getDepartureTime().format(formatter)),
                () -> assertEquals(50, dbTicket.getAvailableTickets())
        );
    }

    @Test
    void shouldHandleDifferentTimeZonesConversion() {
        ZonedDateTime departure = ZonedDateTime.of(2024, 6, 1, 15, 30, 0, 0, ZoneId.of("Europe/Moscow"));
        ZonedDateTime arrival = departure.plusHours(5);

        Ticket saved = ticketDao.save(createTestTicket(departure, arrival, 20));
        Optional<Ticket> found = ticketDao.findByIdWithDetails(saved.getId());

        assertTrue(found.isPresent());
        Ticket dbTicket = found.get();

        ZonedDateTime expectedDepartureUTC = departure.withZoneSameInstant(utc);
        assertAll(
                () -> assertEquals(expectedDepartureUTC.toInstant(), dbTicket.getDepartureTime().toInstant()),
                () -> assertEquals("2024-06-01T12:30", dbTicket.getDepartureTime().format(formatter))
        );
    }

    @Test
    void shouldImplementCursorPaginationCorrectly() {
        List<ZonedDateTime> departureTimes = List.of(
                ZonedDateTime.of(2024, 3, 1, 10, 0, 0, 0, utc),
                ZonedDateTime.of(2024, 3, 1, 11, 0, 0, 0, utc),
                ZonedDateTime.of(2024, 3, 1, 11, 0, 0, 0, utc), // одинаковое время, разные id
                ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, utc)
        );

        departureTimes.forEach(time ->
                ticketDao.save(createTestTicket(time, time.plusHours(4), 100))
        );

        // First page
        List<Ticket> page1 = ticketDao.findTickets(
                1, 1,
                ZonedDateTime.of(2024, 3, 1, 0, 0, 0, 0, utc),
                ZonedDateTime.of(2024, 3, 1, 23, 59, 59, 0, utc),
                ZonedDateTime.of(2024, 3, 1, 9, 0, 0, 0, utc),
                0,
                2
        );

        assertEquals(2, page1.size());
        assertEquals("2024-03-01T10:00", page1.get(0).getDepartureTime().format(formatter));
        assertEquals("2024-03-01T11:00", page1.get(1).getDepartureTime().format(formatter));

        // Second page with cursor
        Ticket lastItem = page1.get(1);
        List<Ticket> page2 = ticketDao.findTickets(
                1, 1,
                ZonedDateTime.of(2024, 3, 1, 0, 0, 0, 0, utc),
                ZonedDateTime.of(2024, 3, 1, 23, 59, 59, 0, utc),
                lastItem.getDepartureTime(),
                lastItem.getId(),
                2
        );

        assertEquals(2, page2.size());
        assertTrue(page2.get(0).getId() > lastItem.getId());
        assertEquals("2024-03-01T11:00", page2.get(0).getDepartureTime().format(formatter));
        assertEquals("2024-03-01T12:00", page2.get(1).getDepartureTime().format(formatter));
    }

    @Test
    void shouldUpdateAvailableTicketsAtomically() {
        Ticket ticket = ticketDao.save(createTestTicket(
                ZonedDateTime.now(utc),
                ZonedDateTime.now(utc).plusHours(2),
                10
        ));

        ticketDao.updateAvailableTickets(ticket.getId(), 5);
        Optional<Ticket> updated = ticketDao.findByIdWithDetails(ticket.getId());

        assertTrue(updated.isPresent());
        assertEquals(5, updated.get().getAvailableTickets());
    }

    @Test
    void shouldReturnEmptyForNonExistingTicket() {
        Optional<Ticket> found = ticketDao.findByIdWithDetails(9999);
        assertFalse(found.isPresent());
    }




}