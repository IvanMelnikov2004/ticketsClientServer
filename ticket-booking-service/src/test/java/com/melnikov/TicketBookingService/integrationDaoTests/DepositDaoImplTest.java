package com.melnikov.TicketBookingService.integrationDaoTests;



import com.melnikov.TicketBookingService.dao.DepositDaoImpl;
import com.melnikov.TicketBookingService.entity.Deposit;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class DepositDaoImplTest {

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
    private DepositDaoImpl depositDao;

    @BeforeEach
    void setUp() {
        // Cleanup and setup database
        jdbcTemplate.execute("DROP TABLE IF EXISTS deposits CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS users CASCADE");

        jdbcTemplate.execute("CREATE TABLE users (" +
                "id SERIAL PRIMARY KEY, " +
                "email VARCHAR(255) NOT NULL UNIQUE)");

        jdbcTemplate.execute("CREATE TABLE deposits (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id BIGINT NOT NULL REFERENCES users(id), " +
                "amount INTEGER NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL)");

        // Insert test user
        jdbcTemplate.update("INSERT INTO users(id, email) VALUES (1, 'user@test.com')");
    }

    private Deposit createTestDeposit() {
        return Deposit.builder()
                .userId(1L)
                .amount(100)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldSaveAndFindDepositById() {
        // Given
        Deposit deposit = createTestDeposit();

        // When
        Deposit saved = depositDao.save(deposit);
        Optional<Deposit> found = depositDao.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        Deposit dbDeposit = found.get();
        assertAll(
                () -> assertEquals(1L, dbDeposit.getUserId()),
                () -> assertEquals(100, dbDeposit.getAmount()),
                () -> assertEquals("PENDING", dbDeposit.getStatus()),
                () -> assertNotNull(dbDeposit.getCreatedAt())
        );
    }

    @Test
    void shouldReturnEmptyForNonExistingDeposit() {
        Optional<Deposit> found = depositDao.findById(999L);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindLast10DepositsOrderedByDate() {
        // Insert 15 deposits
        for (int i = 0; i < 15; i++) {
            Deposit deposit = createTestDeposit();
            deposit.setCreatedAt(LocalDateTime.now().minusMinutes(i));
            depositDao.save(deposit);
        }

        List<Deposit> deposits = depositDao.findLast10ByUserId(1L);

        assertEquals(10, deposits.size());
        // Verify ordering (newest first)
        for (int i = 0; i < deposits.size() - 1; i++) {
            assertTrue(deposits.get(i).getCreatedAt()
                    .isAfter(deposits.get(i + 1).getCreatedAt()));
        }
    }

    @Test
    void shouldUpdateDepositStatus() {
        Deposit deposit = depositDao.save(createTestDeposit());

        depositDao.updateStatus(deposit.getId(), "COMPLETED");

        Optional<Deposit> updated = depositDao.findById(deposit.getId());
        assertTrue(updated.isPresent());
        assertEquals("COMPLETED", updated.get().getStatus());
    }

    @Test
    void shouldHandleUserForeignKeyConstraint() {
        Deposit deposit = Deposit.builder()
                .userId(999L) // Non-existing user
                .amount(200)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> depositDao.save(deposit));
    }

    @Test
    void shouldHandleNullValues() {
        Deposit deposit = createTestDeposit();
        deposit.setStatus(null);

        assertThrows(DataIntegrityViolationException.class,
                () -> depositDao.save(deposit));
    }

    @Test
    void shouldHandleLargeAmountValues() {
        Deposit deposit = createTestDeposit();
        deposit.setAmount(Integer.MAX_VALUE);

        Deposit saved = depositDao.save(deposit);
        Optional<Deposit> found = depositDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(Integer.MAX_VALUE, found.get().getAmount());
    }

    @Test
    void shouldReturnEmptyListForUserWithoutDeposits() {
        List<Deposit> deposits = depositDao.findLast10ByUserId(1L);
        assertTrue(deposits.isEmpty());
    }

    @Test
    void shouldHandleMultipleStatusUpdates() {
        Deposit deposit = depositDao.save(createTestDeposit());

        depositDao.updateStatus(deposit.getId(), "PROCESSING");
        depositDao.updateStatus(deposit.getId(), "COMPLETED");
        depositDao.updateStatus(deposit.getId(), "CANCELLED");

        Optional<Deposit> updated = depositDao.findById(deposit.getId());
        assertTrue(updated.isPresent());
        assertEquals("CANCELLED", updated.get().getStatus());
    }
}