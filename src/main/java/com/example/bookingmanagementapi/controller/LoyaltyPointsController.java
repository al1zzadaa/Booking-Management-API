package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/history/{id}")
    public List<LoyaltyPointResponse> getPointsByUser(@PathVariable Long id) {
        return loyaltyPointService.getHistory(id);
    }

    @GetMapping("/{userId}")
    public Integer getPoints(@PathVariable Long userId){
        return loyaltyPointService.getPoints(userId);
    }

}
