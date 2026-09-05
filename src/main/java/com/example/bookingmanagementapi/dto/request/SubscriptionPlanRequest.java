package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.SubscriptionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanRequest {

    @NotNull
    private SubscriptionType subscriptionType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer durationDays;
}
