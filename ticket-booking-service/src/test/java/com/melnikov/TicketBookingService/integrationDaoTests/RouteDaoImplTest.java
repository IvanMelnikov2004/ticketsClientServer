package com.melnikov.TicketBookingService.integrationDaoTests;



import com.melnikov.TicketBookingService.dao.RouteDaoImpl;
import com.melnikov.TicketBookingService.entity.Route;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class RouteDaoImplTest {

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
    private RouteDaoImpl routeDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS routes CASCADE");
        jdbcTemplate.execute("CREATE TABLE routes (" +
                "id SERIAL PRIMARY KEY, " +
                "departure_city VARCHAR(255) NOT NULL, " +
                "arrival_city VARCHAR(255) NOT NULL, " +
                "CONSTRAINT unique_route UNIQUE (departure_city, arrival_city))");
    }

    @Test
    void shouldCreateAndFindRoute() {
        // Create new route
        Integer id = routeDao.createRoute("Москва", "Казань");

        // Test findById
        Optional<Route> foundRoute = routeDao.findById(id);
        assertTrue(foundRoute.isPresent());
        assertEquals("Москва", foundRoute.get().getDepartureCity());
        assertEquals("Казань", foundRoute.get().getArrivalCity());

        // Test findIdByCities
        Optional<Integer> foundId = routeDao.findIdByCities("Москва", "Казань");
        assertTrue(foundId.isPresent());
        assertEquals(id, foundId.get());

        // Test existsByCities
        assertTrue(routeDao.existsByCities("Москва", "Казань"));
    }


    @Test
    void shouldReturnEmptyForNonExistingRoutes() {
        // Test findById
        assertFalse(routeDao.findById(999).isPresent());

        // Test findIdByCities
        assertFalse(routeDao.findIdByCities("Берлин", "Мюнхен").isPresent());

        // Test existsByCities
        assertFalse(routeDao.existsByCities("Мадрид", "Лисабон"));
    }



    @Test
    void shouldPreventInvalidRoutes() {
        // Test null values
        assertThrows(DataIntegrityViolationException.class,
                () -> routeDao.createRoute(null, "Париж"));

        assertThrows(DataIntegrityViolationException.class,
                () -> routeDao.createRoute("Лондон", null));
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String departure = "Москва";
        String arrival = "Крым";

        Integer id = routeDao.createRoute(departure, arrival);

        Optional<Route> route = routeDao.findById(id);
        assertTrue(route.isPresent());
        assertEquals(departure, route.get().getDepartureCity());
        assertEquals(arrival, route.get().getArrivalCity());
    }

    @Test
    void shouldHandleLongCityNames() {
        String longCityName = "A".repeat(255);

        Integer id = routeDao.createRoute(longCityName, longCityName);

        Optional<Route> route = routeDao.findById(id);
        assertTrue(route.isPresent());
        assertEquals(longCityName, route.get().getDepartureCity());
    }

    @Test
    void shouldFailForTooLongCityNames() {
        String tooLong = "A".repeat(256);

        assertThrows(DataIntegrityViolationException.class,
                () -> routeDao.createRoute(tooLong, "City"));
    }
}