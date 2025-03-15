package com.melnikov.TicketBookingService.services;



import com.melnikov.TicketBookingService.dao.DepositDao;
import com.melnikov.TicketBookingService.dao.DepositDaoImpl;
import com.melnikov.TicketBookingService.dao.UserDao;
import com.melnikov.TicketBookingService.dao.UserDaoImpl;
import com.melnikov.TicketBookingService.entity.Deposit;
import com.melnikov.TicketBookingService.entity.User;

import com.melnikov.TicketBookingService.exception.DepositAlreadyProcessedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepositService {
    private final DepositDao depositDao;
    private final UserDao userDao;

    public DepositService(DepositDaoImpl depositDao, UserDaoImpl userDao) {
        this.depositDao = depositDao;
        this.userDao = userDao;

    }

    @Transactional
    public void createDeposit(Long userId, Integer amount) {
        // Генерируем уникальный идентификатор транзакции


        Deposit deposit = Deposit.builder()
                .userId(userId)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .status("pending")
                .build();
        depositDao.save(deposit);

    }

    @Transactional
    public void confirmDeposit(Long depositId, String status) {
        Optional<Deposit> depositOpt = depositDao.findById(depositId);
        if (depositOpt.isEmpty()) {
            throw new RuntimeException("Deposit not found");
        }

        Deposit deposit = depositOpt.get();
        if (!"pending".equals(deposit.getStatus())) {
            throw new DepositAlreadyProcessedException("deposit is already processed");
        }

        if ("completed".equals(status)) {
            // Обновляем баланс пользователя
            User user = userDao.findById(deposit.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            double newBalance = user.getBalance() + deposit.getAmount();
            boolean updated = userDao.updateBalanceById(user.getId(), newBalance);

            if (!updated) {
                throw new RuntimeException("Failed to update user balance");
            }
        } else if (!"failed".equals(status)) {
            throw new RuntimeException("Invalid status");
        }

        // Обновляем статус пополнения
        depositDao.updateStatus(depositId, status);
    }

    @Transactional(readOnly = true)
    public List<Deposit> getLastDeposits(Long userId) {
        return depositDao.findLast10ByUserId(userId);
    }

}

