package com.melnikov.TicketBookingService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String firstname;
    private String lastname;
    private LocalDate birthDate;
    private Integer balance;
    private Integer roleId;

    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
