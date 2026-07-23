package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import com.example.bookingmanagementapi.enums.LoyaltyType;
import com.example.bookingmanagementapi.mapper.LoyaltyPointMapper;
import com.example.bookingmanagementapi.repository.LoyaltyPointRepository;
import com.example.bookingmanagementapi.service.LoyaltyPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final LoyaltyPointMapper loyaltyPointMapper;

    @Override
    public void addPoints(LoyaltyPointRequest loyaltyPointRequest) {
        LoyaltyPointEntity loyaltyPointEntity = loyaltyPointMapper.toEntity(loyaltyPointRequest);

        loyaltyPointEntity.setType(LoyaltyType.EARN);

        loyaltyPointRepository.save(loyaltyPointEntity);
    }

    @Override
    public void spendPoints(LoyaltyPointRequest loyaltyPointRequest) {
        LoyaltyPointEntity loyaltyPointEntity = loyaltyPointMapper.toEntity(loyaltyPointRequest);

        loyaltyPointEntity.setType(LoyaltyType.SPEND);

        loyaltyPointRepository.save(loyaltyPointEntity);
    }

    @Override
    public Integer getPoints(Long userId) {
        Integer earned = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.EARN
        );

        Integer spent = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.SPEND
        );

        return earned - spent;
    }

    @Override
    public List<LoyaltyPointResponse> getHistory(Long userId) {
        List<LoyaltyPointEntity> loyaltyPointEntities = loyaltyPointRepository.findAllByUserId(userId);

        return loyaltyPointMapper.toListDto(loyaltyPointEntities);
    }
}
