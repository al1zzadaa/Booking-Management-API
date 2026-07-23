package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.request.UpdateSubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionPlanResponse;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import com.example.bookingmanagementapi.mapper.SubscriptionPlanMapper;
import com.example.bookingmanagementapi.repository.SubscriptionPlanRepository;
import com.example.bookingmanagementapi.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public void addSubscription(SubscriptionPlanRequest subscriptionPlanRequest) {
        subscriptionPlanRepository.save(subscriptionPlanMapper.toEntity(subscriptionPlanRequest));
    }

    @Override
    public void deleteSubscription(Long id) {
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(id)
                .orElseThrow(null);

        subscriptionPlanRepository.deleteById(id);
    }

    @Override
    public void updateSubscription(Long id, UpdateSubscriptionPlanRequest updateSubscriptionPlanRequest) {
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(id)
                .orElseThrow(null);

        subscriptionPlanMapper.update(updateSubscriptionPlanRequest,subscriptionPlanEntity);

        subscriptionPlanRepository.save(subscriptionPlanEntity);
    }

    @Override
    public SubscriptionPlanResponse getSubscription(Long id) {
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(id)
                .orElseThrow(null);

        return subscriptionPlanMapper.toDto(subscriptionPlanEntity);
    }

    @Override
    public List<SubscriptionPlanResponse> getSubscriptions() {
        List<SubscriptionPlanEntity> subscriptionPlanEntities = subscriptionPlanRepository.findAll();

        return subscriptionPlanMapper.toListDto(subscriptionPlanEntities);
    }

    @Override
    public void activate(Long id) {
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(id)
                .orElseThrow(null);

        subscriptionPlanEntity.setActive(true);
        subscriptionPlanRepository.save(subscriptionPlanEntity);
    }

    @Override
    public void deactivate(Long id) {
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(id)
                .orElseThrow(null);

        subscriptionPlanEntity.setActive(false);
        subscriptionPlanRepository.save(subscriptionPlanEntity);
    }
}
