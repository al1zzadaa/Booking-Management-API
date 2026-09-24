package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    void subscribe(Long userId, SubscriptionRequest subscriptionRequest);

    SubscriptionResponse getCurrentSubscription(Long userId);

    void cancel(Long userId);

    boolean isActive(Long userId);

    void renew(Long userId, SubscriptionRequest subscriptionRequest);

    void enableAutoRenew(Long userId);

    void disableAutoRenew(Long userId);

    void deactivateExpiredSubscriptions();
}
