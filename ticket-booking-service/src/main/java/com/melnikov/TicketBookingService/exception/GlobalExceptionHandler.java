package com.melnikov.TicketBookingService.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.melnikov.TicketBookingService.dto.ErrorResponseDto;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.security.auth.message.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("USER_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto("AUTH_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorCode = "INVALID_REQUEST";
        String errorMessage = "Request body is missing or invalid";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            if (ife.getTargetType() != null && ZonedDateTime.class.isAssignableFrom(ife.getTargetType())) {
                errorCode = "INVALID_DATE_FORMAT";
                errorMessage = "Invalid date format. Use ISO-8601: 'yyyy-MM-ddTHH:mm:ssZ' (e.g. 2024-03-15T14:30:00+03:00)";
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(errorCode, errorMessage));
    }

    @ExceptionHandler({
            ExpiredJwtException.class,
            io.jsonwebtoken.security.SignatureException.class,
            io.jsonwebtoken.MalformedJwtException.class,
            io.jsonwebtoken.UnsupportedJwtException.class
    })
    public ResponseEntity<ErrorResponseDto> handleJwtExceptions(Exception ex) {
        String errorCode = "INVALID_TOKEN";
        String message = "Authentication error";

        if (ex instanceof ExpiredJwtException) {
            errorCode = "TOKEN_EXPIRED";
            message = "Token has expired";
        } else if (ex instanceof SignatureException) {
            message = "Invalid token signature";
        } else if (ex instanceof MalformedJwtException) {
            message = "Malformed token";
        } else if (ex instanceof UnsupportedJwtException) {
            message = "Unsupported token type";
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(errorCode, message));
    }

    @ExceptionHandler(DepositNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDepositNotFound(DepositNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("DEPOSIT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DepositAlreadyProcessedException.class)
    public ResponseEntity<ErrorResponseDto> handleDepositAlreadyProcessed(DepositAlreadyProcessedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("DEPOSIT_ALREADY_PROCESSED", ex.getMessage()));
    }

    @ExceptionHandler(IncorrectOldPasswordException.class)
    public ResponseEntity<ErrorResponseDto> handleIncorrectOldPassword(IncorrectOldPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("INCORRECT_OLD_PASSWORD", ex.getMessage()));
    }

    // Добавленный обработчик для IllegalArgumentException с сообщением "Route not found"
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        if ("Route not found".equals(ex.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto("ROUTE_NOT_FOUND", ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("ILLEGAL_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> handleRateLimiterException(RequestNotPermitted ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too many requests. Please try again later.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("INTERNAL_ERROR", "Internal server error"));
    }
}
