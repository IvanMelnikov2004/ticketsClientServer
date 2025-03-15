package com.melnikov.TicketBookingService.services;

import com.melnikov.TicketBookingService.dao.UserDao;
import com.melnikov.TicketBookingService.dto.NewPasswordRequestDto;
import com.melnikov.TicketBookingService.entity.User;
import com.melnikov.TicketBookingService.exception.IncorrectOldPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> changePassword(Long userId, NewPasswordRequestDto newPasswordRequestDto) {
        String newPassword = newPasswordRequestDto.getNewPassword();
        String oldPassword = newPasswordRequestDto.getOldPassword();
        String hashedOldPassword = passwordEncoder.encode(newPassword);
        String hashedNewPassword = passwordEncoder.encode(oldPassword);
        User user = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String hashedCurPassword = user.getPasswordHash();
        if (!hashedCurPassword.equals(hashedOldPassword)){
            throw new IncorrectOldPasswordException("Old password does not match");
        }
        boolean isChanged = userDao.updatePasswordById(userId, hashedNewPassword);

        if (isChanged) {
            return ResponseEntity.ok("Password updated successfully");
        }
        throw new RuntimeException("User not found");
    }

    public User getUserInfo(Long userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}