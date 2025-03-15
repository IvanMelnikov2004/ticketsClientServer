package com.melnikov.TicketBookingService.services;

import com.melnikov.TicketBookingService.entity.User;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    public String generateAccessToken(User user) {
        log.debug("Generating access token for user: {}, roleId: {}", user.getEmail(), user.getRoleId());

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("roleId", user.getRoleId()) // roleId 1 - admin, roleId 2 - user
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8)) // FIX
                .compact();

        log.debug("Generated token: {}", token);
        return token;
    }

    public String generateRefreshToken(User user) {
        log.debug("Generating refresh token for user: {}", user.getEmail());

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8)) // FIX
                .compact();
    }

    public Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8)) // FIX
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid JWT format! Token: {}", token);
                return false;
            }

            String header = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String payload = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            log.debug("Decoded JWT Header: {}", header);
            log.debug("Decoded JWT Payload: {}", payload);

            Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8)) // FIX
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8)) // FIX
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
