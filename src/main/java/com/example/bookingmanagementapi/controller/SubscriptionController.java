package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public void subscribe(@RequestBody SubscriptionRequest subscriptionRequest) {
        subscriptionService.subscribe(subscriptionRequest);
    }

    @GetMapping("/current")
    public SubscriptionResponse getCurrentSubscription(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return subscriptionService.getCurrentSubscription(user.getId());
    }

    @GetMapping("/active")
    public boolean isActive(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return subscriptionService.isActive(user.getId());
    }

    @PostMapping("/renew")
    public void renew(@RequestBody SubscriptionRequest subscriptionRequest) {
        subscriptionService.renew(subscriptionRequest);
    }
}
