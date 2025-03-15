package com.melnikov.TicketBookingService.exception;

public class DepositAlreadyProcessedException extends RuntimeException {
    public DepositAlreadyProcessedException(String message) {
        super(message);
    }
}
