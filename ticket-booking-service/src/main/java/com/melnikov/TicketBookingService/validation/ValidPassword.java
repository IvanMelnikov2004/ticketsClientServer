package com.melnikov.TicketBookingService.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Пароль должен содержать от 8 до 20 символов, хотя бы одну букву, цифру и спецсимвол";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

