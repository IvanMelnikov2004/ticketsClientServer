package com.melnikov.TicketBookingService.controllers;

import com.melnikov.TicketBookingService.dto.*;
import com.melnikov.TicketBookingService.services.AuthService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "API для регистрации, логина и обновления токена")
@RequestMapping("/auth")
@RestController
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Регистрация пользователя",
            description = "Создает нового пользователя на основе переданных данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неправильный формат запроса или валидация не пройдена")
            }
    )
    @PostMapping("/register")
    @RateLimiter(name = "defaultLimiter")
    public AuthResponseDto register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequestDto.class))
            )
            @Valid @RequestBody RegisterRequestDto request) {
        return authService.registerUser(request);
    }

    @Operation(
            summary = "Аутентификация пользователя",
            description = "Выполняет логин и возвращает JWT-токен",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
            }
    )
    @PostMapping("/login")
    @RateLimiter(name = "defaultLimiter")
    public AuthResponseDto login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для входа",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            )
            @Valid @RequestBody LoginRequestDto request) throws AuthException {
        return authService.loginUser(request);
    }

    @Operation(
            summary = "Обновление токена",
            description = "Получает новый JWT по refresh-токену",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Новый токен выдан",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = RefreshTokenResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неверный или просроченный refresh-токен")
            }
    )
    @PostMapping("/refresh")
    @RateLimiter(name = "defaultLimiter")
    public ResponseEntity<RefreshTokenResponseDto> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh-токен для обновления JWT",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequestDto.class))
            )
            @Valid @RequestBody RefreshTokenRequestDto request) throws AuthException {
        String newToken = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new RefreshTokenResponseDto(newToken));
    }
}