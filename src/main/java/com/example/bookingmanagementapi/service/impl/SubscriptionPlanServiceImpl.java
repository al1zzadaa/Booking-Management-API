package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionPlanResponse;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.SubscriptionPlanMapper;
import com.example.bookingmanagementapi.repository.SubscriptionPlanRepository;
import com.example.bookingmanagementapi.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        var subscriptionPlan = getSubscriptionPlanEntity(id);

        subscriptionPlanRepository.delete(subscriptionPlan);
    }

    @Override
    public void updateSubscription(Long id, SubscriptionPlanRequest updateSubscriptionPlanRequest) {
        var subscriptionPlanEntity = getSubscriptionPlanEntity(id);

        subscriptionPlanMapper.update(updateSubscriptionPlanRequest, subscriptionPlanEntity);

        subscriptionPlanRepository.save(subscriptionPlanEntity);
    }

    @Override
    public SubscriptionPlanResponse getSubscription(Long id) {
        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription Plan Not Found"));

        return subscriptionPlanMapper.toDto(subscriptionPlanEntity);
    }

    @Override
    public List<SubscriptionPlanResponse> getSubscriptions() {
        List<SubscriptionPlanEntity> subscriptionPlanEntities = subscriptionPlanRepository.findAll();

        return subscriptionPlanMapper.toListDto(subscriptionPlanEntities);
    }

    @Override
    public void activate(Long id) {
        var subscriptionPlanEntity = getSubscriptionPlanEntity(id);

        subscriptionPlanEntity.setActive(true);
        subscriptionPlanRepository.save(subscriptionPlanEntity);

        log.info("Subscription plan with id {} activated", id);
    }

    @Override
    public void deactivate(Long id) {

        var subscriptionPlanEntity = getSubscriptionPlanEntity(id);

        subscriptionPlanEntity.setActive(false);
        subscriptionPlanRepository.save(subscriptionPlanEntity);

        log.info("Subscription plan with id {} deactivated", id);
    }

    private SubscriptionPlanEntity getSubscriptionPlanEntity(Long id) {

        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subscription Plan Not Found"));

    }

}
