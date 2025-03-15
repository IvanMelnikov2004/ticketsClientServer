package com.melnikov.TicketBookingService.services;

import com.melnikov.TicketBookingService.dao.RefreshTokenDao;
import com.melnikov.TicketBookingService.dao.RefreshTokenDaoImpl;
import com.melnikov.TicketBookingService.dao.UserDao;
import com.melnikov.TicketBookingService.dao.UserDaoImpl;
import com.melnikov.TicketBookingService.dto.AuthResponseDto;
import com.melnikov.TicketBookingService.dto.LoginRequestDto;
import com.melnikov.TicketBookingService.dto.RegisterRequestDto;
import com.melnikov.TicketBookingService.entity.RefreshToken;
import com.melnikov.TicketBookingService.entity.User;
import com.melnikov.TicketBookingService.exception.UserAlreadyExistsException;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class AuthService {

    private final UserDao userDao;
    private final RefreshTokenDao refreshTokenDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserDaoImpl userDao, RefreshTokenDaoImpl refreshTokenDao,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userDao = userDao;
        this.refreshTokenDao = refreshTokenDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Transactional
    public AuthResponseDto registerUser(RegisterRequestDto request){
        if (userDao.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .birthDate(request.getBirthDate())
                .balance(0)
                .roleId(2)
                .build();

        user = userDao.save(user);

        return generateAndSaveTokens(user);
    }



    @Transactional
    public AuthResponseDto loginUser(LoginRequestDto request) throws AuthException {
        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        refreshTokenDao.deleteByUserId(user.getId());
        return generateAndSaveTokens(user);
    }



    @Transactional
    public String refreshToken(String refreshToken) throws AuthException {
        log.info("Attempting to refresh token. Received token: {}", passwordEncoder.encode(refreshToken));

        // 1. Поиск токена в БД
        log.info("Searching for token in database...");
        RefreshToken storedToken = refreshTokenDao.findByToken(hashRefreshToken(refreshToken))
                .orElseThrow(() -> {
                    log.error("Token not found in database");
                    return new AuthException("Invalid refresh token");
                });
        log.info("Found token: ID={}, UserID={}, Expiry={}",
                storedToken.getId(), storedToken.getUserId(), storedToken.getExpiryDate());

        // 2. Проверка срока действия
        LocalDateTime now = LocalDateTime.now();
        log.info("Current time: {}, Token expiry: {}", now, storedToken.getExpiryDate());
        if (storedToken.getExpiryDate().isBefore(now)) {
            log.info("Token expired. Deleting all user tokens. UserID: {}", storedToken.getUserId());
            refreshTokenDao.deleteByUserId(storedToken.getUserId());
            throw new AuthException("Refresh token expired");
        }

        // 3. Поиск пользователя
        log.info("Searching user with ID: {}", storedToken.getUserId());
        User user = userDao.findById(storedToken.getUserId())
                .orElseThrow(() -> {
                    log.info("User not found. UserID: {}", storedToken.getUserId());
                    return new AuthException("User not found");
                });

        return jwtService.generateAccessToken(user);

    }

    private AuthResponseDto generateAndSaveTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken newRefreshToken = RefreshToken.builder().
                userId(user.getId())
                .token(hashRefreshToken(refreshToken))
                .expiryDate(jwtService.extractExpiration(refreshToken).toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .build();

        refreshTokenDao.save(newRefreshToken);

        return new AuthResponseDto(accessToken, refreshToken);
    }

    // Добавьте в ваш AuthServiceImpl
    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
