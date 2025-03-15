package com.melnikov.TicketBookingService.validation;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {
    String message() default "Некорректный возраст";

    int minAge() default 14;
    int maxAge() default 100;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}