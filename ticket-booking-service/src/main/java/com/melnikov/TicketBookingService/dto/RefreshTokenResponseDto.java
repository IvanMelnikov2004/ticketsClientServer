package com.melnikov.TicketBookingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenResponseDto {
    private String accessToken;
}
