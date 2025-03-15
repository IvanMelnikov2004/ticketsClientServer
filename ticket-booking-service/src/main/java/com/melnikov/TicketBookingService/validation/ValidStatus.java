package com.melnikov.TicketBookingService.validation;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatus {
    String message() default "Статус должен быть 'completed' или 'failed'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

