package com.melnikov.TicketBookingService.controllers;



import com.melnikov.TicketBookingService.dto.DepositConfirmDto;
import com.melnikov.TicketBookingService.dto.DepositRequestDto;
import com.melnikov.TicketBookingService.entity.Deposit;
import com.melnikov.TicketBookingService.services.DepositService;
import com.melnikov.TicketBookingService.services.JwtService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deposits")
public class DepositController {
    private final DepositService depositService;
    private final JwtService jwtService;

    public DepositController(DepositService depositService, JwtService jwtService) {
        this.depositService = depositService;
        this.jwtService = jwtService;
    }

    @PostMapping("/create")
    @RateLimiter(name = "defaultLimiter")
    public ResponseEntity<?> createDeposit(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid DepositRequestDto depositRequestDto) {

        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        Long userId = claims.get("id", Long.class);

        depositService.createDeposit(userId, depositRequestDto.getAmount());
        return ResponseEntity.ok("Deposit created successfully.");
    }

    @PostMapping("/confirm")
    @RateLimiter(name = "defaultLimiter")
    public ResponseEntity<?> confirmDeposit(@RequestBody @Valid DepositConfirmDto request) {
        depositService.confirmDeposit(request.getDepositId(), request.getStatus());
        return ResponseEntity.ok("Deposit " + request.getStatus() + " successfully.");
    }


    @GetMapping("/last")
    @RateLimiter(name = "defaultLimiter")
    public ResponseEntity<?> getLastDeposits(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtService.extractClaims(token);
        Long userId = claims.get("id", Long.class);

        return ResponseEntity.ok(depositService.getLastDeposits(userId));
    }

}

