package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface LoyaltyPointService {

    void addPoints(LoyaltyPointRequest loyaltyPointRequest);

    void removePoints(LoyaltyPointRequest loyaltyPointRequest);

    Integer getPoints(Long userId);

    List<LoyaltyPointResponse> getHistory(Long userId);


    int calculateEarnedPoints(BigDecimal amount);

//    int calculateRemovedPoints(BigDecimal amount);

    void earnPoints(
            Long userId,
            BigDecimal amount,
            String description
    );

    void cancelPoints(AccountEntity account,
                      Long userId,
                      BigDecimal amount,
                      String description
    );

    void usePoints(Long accountId, Integer points, String description);

    BigDecimal pointValue(Integer pointsAsAmount);
}
