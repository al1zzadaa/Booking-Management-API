package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;

import java.util.List;

public interface LoyaltyPointService {

    void addPoints(LoyaltyPointRequest loyaltyPointRequest);

    void spendPoints(LoyaltyPointRequest loyaltyPointRequest);

    Integer getPoints(Long userId);

    List<LoyaltyPointResponse> getHistory(Long userId);

}
