package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/loyalty-points")
public class LoyaltyPointsController {

    private final LoyaltyPointService loyaltyPointService;

    @PostMapping("/add-points")
    public void addPoints(@RequestBody LoyaltyPointRequest loyaltyPointRequest) {
        loyaltyPointService.addPoints(loyaltyPointRequest);
    }

    @PostMapping("/delete-points")
    public void removePoints(@RequestBody LoyaltyPointRequest loyaltyPointRequest) {
        loyaltyPointService.removePoints(loyaltyPointRequest);
    }

    @GetMapping
    public Integer getPoints(@AuthenticationPrincipal CustomUserDetails user) {
        return loyaltyPointService.getPoints(user.getId());
    }

    @GetMapping("/history")
    public List<LoyaltyPointResponse> getHistory(@AuthenticationPrincipal CustomUserDetails user) {
        return loyaltyPointService.getHistory(user.getId());
    }

}
