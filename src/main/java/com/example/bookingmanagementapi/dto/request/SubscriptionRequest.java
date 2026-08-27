package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.PaymentMethods;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotNull
    private Long planId;
    private Long accountId;
}
