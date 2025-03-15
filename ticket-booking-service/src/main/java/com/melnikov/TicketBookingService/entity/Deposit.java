package com.melnikov.TicketBookingService.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Deposit {
    private Long id;
    private Long userId;
    private Integer amount;
    private String status;
    private LocalDateTime createdAt;

}
