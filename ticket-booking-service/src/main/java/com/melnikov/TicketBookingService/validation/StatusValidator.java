package com.melnikov.TicketBookingService.validation;



import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class StatusValidator implements ConstraintValidator<ValidStatus, String> {
    private static final Set<String> VALID_STATUSES = Set.of("completed", "failed");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && VALID_STATUSES.contains(value);
    }
}
