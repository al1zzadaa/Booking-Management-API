package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.SubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionPlanResponse;
import com.example.bookingmanagementapi.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription-plan")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping
    public void addSubscription(
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {
        subscriptionPlanService.addSubscription(request);
    }

    @GetMapping
    public List<SubscriptionPlanResponse> getSubscriptions() {
        return subscriptionPlanService.getSubscriptions();
    }

    @GetMapping("/{id}")
    public SubscriptionPlanResponse getSubscription(
            @PathVariable Long id
    ) {
        return subscriptionPlanService.getSubscription(id);
    }

    @PutMapping("/{id}")
    public void updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {
        subscriptionPlanService.updateSubscription(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteSubscription(
            @PathVariable Long id
    ) {
        subscriptionPlanService.deleteSubscription(id);
    }

    @PatchMapping("/{id}/activate")
    public void activate(
            @PathVariable Long id
    ) {
        subscriptionPlanService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(
            @PathVariable Long id
    ) {
        subscriptionPlanService.deactivate(id);
    }
}
