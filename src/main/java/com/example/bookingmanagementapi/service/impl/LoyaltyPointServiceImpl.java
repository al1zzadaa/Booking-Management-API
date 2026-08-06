package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.LoyaltyType;
import com.example.bookingmanagementapi.mapper.LoyaltyPointMapper;
import com.example.bookingmanagementapi.repository.LoyaltyPointRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final LoyaltyPointMapper loyaltyPointMapper;
    private final UserRepository userRepository;
    @Value("${loyalty.earning-rate}")
    private BigDecimal earningRate;

    @Value("${loyalty.point-value}")
    private BigDecimal pointValue;

    @Override
    public void addPoints(LoyaltyPointRequest loyaltyPointRequest) {

        UserEntity user = userRepository.findById(loyaltyPointRequest.getUserId())
                .orElseThrow(null);

        LoyaltyPointEntity loyaltyPointEntity = LoyaltyPointEntity.builder()
                .points(loyaltyPointRequest.getPoints())
                .user(user)
                .description(loyaltyPointRequest.getDescription())
                .type(LoyaltyType.EARN)
                .build();


        loyaltyPointRepository.save(loyaltyPointEntity);
    }

    @Override
    public void removePoints(LoyaltyPointRequest loyaltyPointRequest) {
        LoyaltyPointEntity loyaltyPointEntity = loyaltyPointMapper.toEntity(loyaltyPointRequest);

        loyaltyPointEntity.setType(LoyaltyType.SPEND);


        loyaltyPointRepository.save(loyaltyPointEntity);
    }

    @Override
    public Integer getPoints(Long userId) {

//        UserEntity user = userRepository.findById(userId).orElseThrow(null);

        Integer earned = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.EARN
        );

        Integer spent = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.SPEND
        );

        Integer refunded = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.REFUND
        );

        return earned + refunded - spent;
    }

    @Override
    public List<LoyaltyPointResponse> getHistory(Long userId) {

        List<LoyaltyPointEntity> loyaltyPointEntities = loyaltyPointRepository.findAllByUserId(userId);

        return loyaltyPointMapper.toListDto(loyaltyPointEntities);
    }

    public int calculateEarnedPoints(BigDecimal amount) {
        return amount
                .multiply(earningRate)
                .setScale(0, RoundingMode.DOWN)
                .intValueExact();
    }

//    public int calculateRemovedPoints(BigDecimal amount) {
//        return amount
//                .multiply(earningRate)
//                .intValue();
//    }

    public void earnPoints(
            Long userId,
            BigDecimal amount,
            String description
    ) {

        Integer points = calculateEarnedPoints(amount);

        var loyaltyPointRequest = LoyaltyPointRequest.builder()
                .userId(userId)
                .points(points)
                .description(description)
                .build();

        addPoints(loyaltyPointRequest);
    }

    public void cancelPoints(
            Long userId,
            BigDecimal amount,
            String description
    ) {
        Integer points = calculateEarnedPoints(amount);

        var loyaltyPointRequest = LoyaltyPointRequest.builder()
                .userId(userId)
                .points(points)
                .description(description)
                .build();

        removePoints(loyaltyPointRequest);
    }
}
