package com.melnikov.TicketBookingService.controllers;

import com.melnikov.TicketBookingService.dto.*;
import com.melnikov.TicketBookingService.services.AuthService;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/auth")
@RestController
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request){
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) throws AuthException {
        return authService.loginUser(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequestDto request) throws AuthException {
        return ResponseEntity.ok(new RefreshTokenResponseDto(authService.refreshToken(request.getRefreshToken())));
    }

}
