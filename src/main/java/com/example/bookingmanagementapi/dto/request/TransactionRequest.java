package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.PaymentMethods;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    @NotNull
    @Positive
    private Long accountId;

    @NotNull
    private TransactionType type;

    @NotNull
    private ReferenceType referenceType;

    @NotNull
    @Positive
    private Long referenceId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    @Length(max = 100)
    private String description;

    @NotNull
    private PaymentMethods paymentMethod;

    @NotNull
    private PaymentStatus paymentStatus;
}




