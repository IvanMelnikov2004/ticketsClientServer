package com.melnikov.TicketBookingService.dto;

import java.time.Instant;

public record ErrorResponseDto(
        String code,
        Object message,  // Может быть строкой или списком
        Instant timestamp
) {
    public ErrorResponseDto(String code, Object message) {
        this(code, message, Instant.now());
    }
}
