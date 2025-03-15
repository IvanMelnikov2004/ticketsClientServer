package com.melnikov.TicketBookingService.configuration;

import com.melnikov.TicketBookingService.dao.UserDaoImpl;
import com.melnikov.TicketBookingService.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AdminInitializer {
    private final UserDaoImpl userDao;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initAdmin() {
        String adminEmail = "admin@example.com"; // Укажите email админа
        if (userDao.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("admin1234!")) // Хешируем пароль
                    .firstname("Admin")
                    .lastname("Admin")
                    .birthDate(LocalDate.ofYearDay(2004, 1)) // Укажите возраст
                    .balance(0)
                    .roleId(1) // ID роли 'admin' из таблицы roles
                    .build();
            userDao.save(admin);
        }
    }
}