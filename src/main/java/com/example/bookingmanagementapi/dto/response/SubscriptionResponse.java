package com.example.bookingmanagementapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionResponse {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean autoRenew;
    private SubscriptionPlanResponse subscriptionPlan;
}
