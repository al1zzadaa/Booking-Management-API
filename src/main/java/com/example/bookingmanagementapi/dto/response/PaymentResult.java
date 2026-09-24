package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.entity.AccountEntity;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentResult{
    private AccountEntity account;
    private BigDecimal finalAmountInUsd;
    private BigDecimal finalAmount;
}