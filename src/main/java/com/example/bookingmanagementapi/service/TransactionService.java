package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
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

    void refundTicket(TicketEntity ticketId, BigDecimal amount);

    void refundBooking(BookingEntity booking, BigDecimal amount);

    void withdraw(WithdrawRequest withdrawRequest);

    void deposit(DepositRequest depositRequest);

    void payForBooking(BookingEntity booking, PaymentRequest paymentRequest);
}
