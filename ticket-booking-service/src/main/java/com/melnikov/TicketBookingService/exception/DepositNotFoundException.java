package com.melnikov.TicketBookingService.exception;



public class DepositNotFoundException extends RuntimeException {
    public DepositNotFoundException(String message) {
        super(message);
    }
}
