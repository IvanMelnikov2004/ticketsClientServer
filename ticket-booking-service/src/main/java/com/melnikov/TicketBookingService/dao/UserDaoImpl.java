package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Optional<User> findByEmail(String email) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE email = ?",
                    new BeanPropertyRowMapper<>(User.class),
                    email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(Long id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE id = ?",
                    new BeanPropertyRowMapper<>(User.class),
                    id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public User save(User user) {
        long id = jdbcTemplate.queryForObject(
                "INSERT INTO users(email, password_hash, firstname, lastname, birth_date, balance, role_id) VALUES(?,?,?,?,?,?,?) RETURNING id",
                Long.class,
                user.getEmail(),
                user.getPasswordHash(),
                user.getFirstname(),
                user.getLastname(),
                user.getBirthDate(),
                user.getBalance(),
                user.getRoleId());
        user.setId(id);
        return user;
    }

    @Override
    public boolean updatePasswordById(Long id, String newPasswordHash) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE users SET password_hash = ? WHERE id = ?",
                newPasswordHash,
                id
        );
        return updatedRows > 0;
    }


    @Override
    public Optional<User> getUserInfoById(Long id) {
        return findById(id);
    }

    public boolean updateBalanceById(Long id, double newBalance) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE users SET balance = ? WHERE id = ?",
                newBalance,
                id
        );
        return updatedRows > 0;
    }
}