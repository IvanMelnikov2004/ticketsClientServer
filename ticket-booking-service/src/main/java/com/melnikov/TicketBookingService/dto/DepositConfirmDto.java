package com.melnikov.TicketBookingService.dto;



import com.melnikov.TicketBookingService.validation.ValidStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositConfirmDto {
    @NotNull(message="depositId не может быть пустым")
    private Long depositId;

    @ValidStatus
    private String status;
}

