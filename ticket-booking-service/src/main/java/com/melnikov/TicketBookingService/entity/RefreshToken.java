package com.melnikov.TicketBookingService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiryDate;

}