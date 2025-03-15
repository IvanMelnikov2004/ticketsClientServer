package com.melnikov.TicketBookingService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private Integer id;
    private String departureCity;
    private String arrivalCity;
}