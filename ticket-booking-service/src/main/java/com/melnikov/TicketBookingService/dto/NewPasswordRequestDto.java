package com.melnikov.TicketBookingService.dto;

import com.melnikov.TicketBookingService.validation.ValidPassword;
import lombok.Data;

@Data
public class NewPasswordRequestDto {
    @ValidPassword
    private String newPassword;

    @ValidPassword
    private String oldPassword;
}
