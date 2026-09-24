package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.entity.FlightBookingEntity;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    void deleteTransaction(Long transactionId);

    TransactionResponse getTransactionById(Long transactionId);

    Page<@NonNull TransactionResponse> getTransactionsByUserId(String email, Pageable  pageable);

    void payForTickets(Long flightBookingId, PaymentRequest paymentRequest);

    void refundTicket(FlightBookingEntity flightBookingEntity, BigDecimal amount);

    void refundBooking(BookingEntity booking, BigDecimal amount);

    void withdraw(WithdrawRequest withdrawRequest);

    void deposit(DepositRequest depositRequest);

    void payForBooking(BookingEntity booking, PaymentRequest paymentRequest);

    void processSubscriptionPayment(SubscriptionRequest subscriptionRequest,
                            Long subscriptionId,
                            SubscriptionPlanEntity subscriptionPlanEntity,
                            String description);
}
