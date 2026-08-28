package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull
    @Positive
    private Long accountId;

    @Size(max = 50)
    private String promoCode;

    @Positive
    private Integer loyaltyPointsToUse;
}