package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.DepositRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.WithdrawRequest;
import com.example.bookingmanagementapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public void deposit(@RequestBody DepositRequest depositRequest) {
        transactionService.deposit(depositRequest);
    }

    @PostMapping("/withdraw")
    public void withdraw(@RequestBody WithdrawRequest withdrawRequest) {
        transactionService.withdraw(withdrawRequest);
    }
}
