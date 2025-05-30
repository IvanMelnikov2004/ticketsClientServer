package com.melnikov.TicketBookingService.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ArrivalAfterDepartureValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArrivalAfterDeparture {
    String message() default "Wrong arrival time or departure time";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
