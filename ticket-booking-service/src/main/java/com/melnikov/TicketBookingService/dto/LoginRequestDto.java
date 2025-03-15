package com.melnikov.TicketBookingService.dto;

import com.melnikov.TicketBookingService.validation.ValidEmail;
import com.melnikov.TicketBookingService.validation.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {

    @ValidEmail
    private String email;

    @ValidPassword
    private String password;
}
