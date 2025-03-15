package com.melnikov.TicketBookingService.validation;

import com.melnikov.TicketBookingService.dto.TicketCreateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ArrivalAfterDepartureValidator
        implements ConstraintValidator<ArrivalAfterDeparture, TicketCreateRequestDto> {

    @Override
    public boolean isValid(TicketCreateRequestDto request, ConstraintValidatorContext context) {
        if (request.getDepartureTime() == null || request.getArrivalTime() == null){
            return false;
        }
        return request.getArrivalTime().isAfter(request.getDepartureTime());
    }
}