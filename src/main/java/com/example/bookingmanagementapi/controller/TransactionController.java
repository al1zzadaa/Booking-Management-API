package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.DepositRequest;
import com.example.bookingmanagementapi.dto.request.WithdrawRequest;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public void deposit(@Valid @RequestBody DepositRequest depositRequest) {
        transactionService.deposit(depositRequest);
    }

    @PostMapping("/withdraw")
    public void withdraw(@Valid @RequestBody WithdrawRequest withdrawRequest) {
        transactionService.withdraw(withdrawRequest);
    }

    @GetMapping("/my")
    public Page<@NonNull TransactionResponse> getMyTransactions(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable) {
        return transactionService.getTransactionsByUserId(
                user.getEmail(),
                pageable
        );
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(
            @PathVariable @Positive Long id) {
        return transactionService.getTransactionById(id);
    }
}
