package com.melnikov.TicketBookingService.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenRequestDto {

    @NotBlank(message="Токен не должен быть пустым")
    private String refreshToken;
}
