package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.enums.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionResponse {
    private Long id;
    //    private AccountEntity account;
    private TransactionType type;
    private ReferenceType referenceType;
    private Long referenceId;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal amountInUsd;
    private String description;
    private PaymentMethods paymentMethod;
    private PaymentStatus paymentStatus;
}
