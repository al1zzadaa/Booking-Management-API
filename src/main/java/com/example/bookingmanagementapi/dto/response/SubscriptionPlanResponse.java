package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.enums.SubscriptionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanResponse {
    private Long id;
    private SubscriptionType subscriptionType;
    private BigDecimal price;
    private Integer durationDays;
    private Boolean active;
}
