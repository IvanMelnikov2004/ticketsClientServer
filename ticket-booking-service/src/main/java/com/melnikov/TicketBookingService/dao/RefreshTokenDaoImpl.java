package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.RefreshToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@Repository
public class RefreshTokenDaoImpl implements RefreshTokenDao{
    private final JdbcTemplate jdbcTemplate;

    public RefreshTokenDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(RefreshToken refreshToken) {
        String sql = "INSERT INTO refresh_tokens (user_id, token, expiry_date) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, refreshToken.getUserId());
            ps.setString(2, refreshToken.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(refreshToken.getExpiryDate()));
            return ps;
        }, keyHolder);

    }

    public Optional<RefreshToken> findByToken(String token) {
        String sql = "SELECT * FROM refresh_tokens WHERE token = ?";
        try {

            RefreshToken refreshToken = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(RefreshToken.class),
                    token
            );
            return Optional.of(refreshToken);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void deleteByToken(String token) {
        String sql = "DELETE FROM refresh_tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }

    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM refresh_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}