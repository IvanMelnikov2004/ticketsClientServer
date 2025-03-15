package com.melnikov.TicketBookingService.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransportType {
    private Integer id;
    private String name;
}
