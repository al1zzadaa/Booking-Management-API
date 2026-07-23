package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.PaymentMethods;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull
    private Long accountId;

    @NotNull
    private TransactionType type;

    @NotNull
    private ReferenceType referenceType;

    @NotNull
    private Long referenceId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private PaymentMethods paymentMethod;

    @NotNull
    private PaymentStatus paymentStatus;
}




