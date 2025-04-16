package com.melnikov.TicketBookingService.integrationDaoTests;



import com.melnikov.TicketBookingService.dao.RefreshTokenDaoImpl;
import com.melnikov.TicketBookingService.entity.RefreshToken;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class RefreshTokenDaoImplTest {

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
    private RefreshTokenDaoImpl refreshTokenDao;

    @BeforeEach
    void setUp() {
        // Cleanup and setup database
        jdbcTemplate.execute("DROP TABLE IF EXISTS refresh_tokens CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS users CASCADE");

        // Create users table for foreign key constraint
        jdbcTemplate.execute("CREATE TABLE users (" +
                "id SERIAL PRIMARY KEY, " +
                "email VARCHAR(255) NOT NULL UNIQUE)");

        jdbcTemplate.execute("CREATE TABLE refresh_tokens (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL REFERENCES users(id), " +
                "token VARCHAR(255) NOT NULL UNIQUE, " +
                "expiry_date TIMESTAMP NOT NULL)");

        // Insert test user
        jdbcTemplate.update("INSERT INTO users(id, email) VALUES (1, 'test@example.com')");
    }

    private RefreshToken createTestToken() {
        return RefreshToken.builder()
                .userId(1L)
                .token("test_token")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void shouldSaveAndFindRefreshToken() {
        // Given
        RefreshToken token = createTestToken();

        // When
        refreshTokenDao.save(token);
        Optional<RefreshToken> found = refreshTokenDao.findByToken("test_token");

        // Then
        assertTrue(found.isPresent());
        RefreshToken dbToken = found.get();
        assertAll(
                () -> assertEquals(1L, dbToken.getUserId()),
                () -> assertEquals("test_token", dbToken.getToken()),
                () -> assertNotNull(dbToken.getExpiryDate())
        );
    }

    @Test
    void shouldNotFindNonExistingToken() {
        Optional<RefreshToken> found = refreshTokenDao.findByToken("non_existing");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDeleteByToken() {
        // Given
        RefreshToken token = createTestToken();
        refreshTokenDao.save(token);

        // When
        refreshTokenDao.deleteByToken("test_token");

        // Then
        Optional<RefreshToken> found = refreshTokenDao.findByToken("test_token");
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDeleteAllTokensForUser() {
        // Given
        refreshTokenDao.save(createTestToken());
        refreshTokenDao.save(RefreshToken.builder()
                .userId(1L)
                .token("another_token")
                .expiryDate(LocalDateTime.now().plusHours(2))
                .build());

        // When
        refreshTokenDao.deleteByUserId(1L);

        // Then
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_tokens WHERE user_id = 1",
                Long.class
        );
        assertEquals(0L, count);
    }

    @Test
    void shouldHandleTokenUniquenessConstraint() {
        // Given
        RefreshToken token1 = createTestToken();
        refreshTokenDao.save(token1);

        // When/Then
        RefreshToken token2 = createTestToken(); // Same token value
        assertThrows(DataIntegrityViolationException.class,
                () -> refreshTokenDao.save(token2));
    }

    @Test
    void shouldHandleForeignConstraint() {
        RefreshToken token = RefreshToken.builder()
                .userId(999L) // Non-existing user
                .token("invalid_token")
                .expiryDate(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> refreshTokenDao.save(token));
    }

    @Test
    void shouldHandleMultipleTokensForUser() {
        // Given
        refreshTokenDao.save(createTestToken());
        refreshTokenDao.save(RefreshToken.builder()
                .userId(1L)
                .token("second_token")
                .expiryDate(LocalDateTime.now().plusHours(2))
                .build());

        // When
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_tokens WHERE user_id = 1",
                Long.class
        );

        // Then
        assertEquals(2L, count);
    }
}