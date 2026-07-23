package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;

public interface SubscriptionService {

    void subscribe(Long userId, Long planId);

    SubscriptionResponse getCurrentSubscription(Long userId);

    void cancel(Long userId);

    boolean isActive(Long userId);

    void renew(Long userId, Long planId);

    void autoRenew();

    void enableAutoRenew(Long userId);

    void disableAutoRenew(Long userId);

}
