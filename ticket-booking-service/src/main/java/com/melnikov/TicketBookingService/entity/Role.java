package com.melnikov.TicketBookingService.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Role {
    private Integer id;
    private Name name;

    public enum Name {
        ADMIN, USER;

        public static Name fromString(String value) {
            return Name.valueOf(value.toUpperCase());
        }
    }
}
