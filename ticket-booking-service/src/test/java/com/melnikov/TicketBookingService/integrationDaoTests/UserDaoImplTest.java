package com.melnikov.TicketBookingService.integrationDaoTests;

import com.melnikov.TicketBookingService.entity.User;
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

import java.time.LocalDate;
import java.util.Optional;
import com.melnikov.TicketBookingService.dao.UserDaoImpl;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class UserDaoImplTest {

    // Запуск PostgreSQL-контейнера. Версию подбираем, например, postgres:15.
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    // Регистрируем свойства подключения для Spring Boot
    @DynamicPropertySource
    public static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDaoImpl userDaoImpl;

    // Инициализация схемы БД перед каждым тестом
    @BeforeEach
    public void setUp() {
        // Удаляем таблицу, если она существует, чтобы каждый тест работал с чистой схемой
        jdbcTemplate.execute("DROP TABLE IF EXISTS users CASCADE");
        jdbcTemplate.execute(
                "CREATE TABLE users (" +
                        "id SERIAL PRIMARY KEY, " +
                        "email VARCHAR(255) NOT NULL, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "firstname VARCHAR(255), " +
                        "lastname VARCHAR(255), " +
                        "birth_date DATE, " +
                        "balance NUMERIC, " +
                        "role_id BIGINT" +
                        ")"
        );
    }

    @Test
    public void testSaveAndFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setBalance(100);
        user.setRoleId(1);

        // Тестируем метод save
        User savedUser = userDaoImpl.save(user);
        assertNotNull(savedUser.getId(), "Идентификатор пользователя не должен быть null после сохранения");

        // Тестируем поиск по email
        Optional<User> foundUserOpt = userDaoImpl.findByEmail("test@example.com");
        assertTrue(foundUserOpt.isPresent(), "Пользователь с заданным email должен быть найден");
        User foundUser = foundUserOpt.get();
        assertEquals("Test", foundUser.getFirstname());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<User> userOptional = userDaoImpl.findById(999L);
        assertFalse(userOptional.isPresent(), "Пользователь с несуществующим ID не должен быть найден");
    }

    @Test
    public void testUpdatePasswordById() {
        User user = new User();
        user.setEmail("update@test.com");
        user.setPasswordHash("oldHash");
        user.setFirstname("Upd");
        user.setLastname("User");
        user.setBirthDate(LocalDate.of(1999, 12, 31));
        user.setBalance(50);
        user.setRoleId(2);
        User savedUser = userDaoImpl.save(user);

        // Обновляем пароль
        boolean updated = userDaoImpl.updatePasswordById(savedUser.getId(), "newHash");
        assertTrue(updated, "Пароль должен быть обновлён");

        // Проверяем, что пароль действительно обновился
        Optional<User> updatedUserOpt = userDaoImpl.findById(savedUser.getId());
        assertTrue(updatedUserOpt.isPresent());
        assertEquals("newHash", updatedUserOpt.get().getPasswordHash());
    }

    @Test
    public void testUpdateBalanceById() {
        User user = new User();
        user.setEmail("balance@test.com");
        user.setPasswordHash("hash");
        user.setFirstname("Balance");
        user.setLastname("User");
        user.setBirthDate(LocalDate.of(2001, 5, 20));
        user.setBalance(75);
        user.setRoleId(3);
        User savedUser = userDaoImpl.save(user);

        // Обновляем баланс
        boolean updated = userDaoImpl.updateBalanceById(savedUser.getId(), 150.0);
        assertTrue(updated, "Баланс должен быть обновлён");

        Optional<User> updatedUserOpt = userDaoImpl.findById(savedUser.getId());
        assertTrue(updatedUserOpt.isPresent());
        assertEquals(150, updatedUserOpt.get().getBalance());
    }
}
