package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.SubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.request.UpdateSubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionPlanResponse;

import java.util.List;

public interface SubscriptionPlanService {

    void addSubscription(SubscriptionPlanRequest subscriptionPlanRequest);

    void deleteSubscription(Long id);

    void updateSubscription(Long  id, UpdateSubscriptionPlanRequest updateSubscriptionPlanRequest);

    SubscriptionPlanResponse getSubscription(Long id);

    List<SubscriptionPlanResponse> getSubscriptions();

    void activate(Long id);

    void deactivate(Long id);

}
