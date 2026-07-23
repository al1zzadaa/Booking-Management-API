package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TransactionRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTransactionRequest;
import com.example.bookingmanagementapi.dto.request.WithdrawRequest;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TransactionService {

    void createTransaction(TransactionRequest transactionRequest);

    void updateTransaction(UpdateTransactionRequest updateTransactionRequest, Long transactionId);

    void deleteTransaction(Long transactionId);

    TransactionResponse getTransactionById(Long transactionId);

    Page<TransactionResponse> getTransactionsByUserId(Long accountId, Pageable  pageable);

    void payForTicket(TicketEntity ticket, PaymentRequest paymentRequest);

    void refund(TicketEntity ticketId,  BigDecimal amount);

    void withdraw(WithdrawRequest withdrawRequest);

    void deposit(PaymentRequest paymentRequest);
}
