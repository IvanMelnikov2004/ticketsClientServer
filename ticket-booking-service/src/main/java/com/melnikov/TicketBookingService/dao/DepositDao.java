package com.melnikov.TicketBookingService.dao;

import com.melnikov.TicketBookingService.entity.Deposit;
import com.melnikov.TicketBookingService.entity.User;

import java.util.List;
import java.util.Optional;

public interface DepositDao {
    List<Deposit> findLast10ByUserId(Long userId);
    Optional<Deposit> findById(Long id);
    Deposit save(Deposit deposit);
    public void updateStatus(Long id, String status);
}
