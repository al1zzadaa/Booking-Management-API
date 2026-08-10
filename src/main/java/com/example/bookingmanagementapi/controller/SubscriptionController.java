package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe-to")
    public void subscribe(@RequestBody SubscriptionRequest subscriptionRequest) {
        subscriptionService.subscribe(subscriptionRequest);
    }

    @GetMapping("/current/{userId}")
    public SubscriptionResponse getCurrentSubscriptionByUserId(@PathVariable Long userId) {
        return subscriptionService.getCurrentSubscription(userId);
    }

    @GetMapping("/isActive/{userId}")
    public boolean isActive(@PathVariable Long userId) {
        return subscriptionService.isActive(userId);
    }

    @PostMapping("/renew")
    public void renew(@RequestBody SubscriptionRequest subscriptionRequest) {
        subscriptionService.renew(subscriptionRequest);
    }
}
