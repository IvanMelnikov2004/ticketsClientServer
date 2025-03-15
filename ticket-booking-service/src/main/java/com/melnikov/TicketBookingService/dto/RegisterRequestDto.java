package com.melnikov.TicketBookingService.dto;

import com.melnikov.TicketBookingService.validation.ValidBirthDate;
import com.melnikov.TicketBookingService.validation.ValidEmail;
import com.melnikov.TicketBookingService.validation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    
    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 25, message = "Длина имени должна быть от 2 до 25 символов")
    private String firstname;

    @NotBlank(message = "Фамилия не должна быть пустой")
    @Size(min = 2, max = 25, message = "Длина фамилии должна быть от 2 до 25 символов")
    private String lastname;

    @ValidEmail
    private String email;

    @ValidPassword
    private String password;

    @NotNull(message = "Дата рождения обязательна")
    @Past(message = "Дата рождения должна быть в прошлом")
    @ValidBirthDate(minAge = 14, maxAge = 100)
    private LocalDate birthDate;
}