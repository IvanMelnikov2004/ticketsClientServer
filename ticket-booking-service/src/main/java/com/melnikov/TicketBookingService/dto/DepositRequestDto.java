package com.melnikov.TicketBookingService.dto;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class DepositRequestDto {
    @NotNull
    @Min(1)
    private Integer amount;




}
