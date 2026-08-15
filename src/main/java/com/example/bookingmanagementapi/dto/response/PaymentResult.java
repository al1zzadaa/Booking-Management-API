package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.entity.AccountEntity;

import java.math.BigDecimal;

public record PaymentResult(
        AccountEntity account,
        BigDecimal finalAmountInUsd,
        BigDecimal finalAmount
) {
}