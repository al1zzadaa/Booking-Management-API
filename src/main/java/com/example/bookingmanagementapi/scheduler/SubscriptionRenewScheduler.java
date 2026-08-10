package com.example.bookingmanagementapi.scheduler;

import com.example.bookingmanagementapi.service.SubscriptionAutoRenewService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionRenewScheduler {
    private final SubscriptionService subscriptionService;
    private final SubscriptionAutoRenewService subscriptionAutoRenewService;

    @Scheduled(fixedRate = 60 * 1000)
    public void runAutoRenew() {
        subscriptionAutoRenewService.autoRenew();

        subscriptionService.deactivateExpiredSubscriptions();
    }
}
