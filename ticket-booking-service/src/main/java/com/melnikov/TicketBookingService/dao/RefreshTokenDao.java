package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenDao {
    void save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUserId(Long userId);
}
