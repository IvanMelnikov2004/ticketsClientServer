package com.melnikov.TicketBookingService.validation;



import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(ValidBirthDate constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
        this.maxAge = constraintAnnotation.maxAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) return true;

        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();

        return age >= minAge && age <= maxAge;
    }
}