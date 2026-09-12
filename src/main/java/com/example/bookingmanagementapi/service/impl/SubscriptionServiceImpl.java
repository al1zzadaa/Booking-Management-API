package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.event.*;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.SubscriptionMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.repository.SubscriptionPlanRepository;
import com.example.bookingmanagementapi.repository.SubscriptionRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import com.example.bookingmanagementapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public void subscribe(Long userId, SubscriptionRequest subscriptionRequest) {

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->  new NotFoundException("User Not Found"));

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findByIdAndActive(subscriptionRequest.getPlanId(), true);

        AccountEntity account = accountRepository.findById(subscriptionRequest.getAccountId()).orElseThrow(() ->  new NotFoundException("Account Not Found"));

        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                .user(user)
                .subscriptionPlan(subscriptionPlanEntity)
                .startDate(LocalDate.now())
                .autoRenewAccount(account)
                .autoRenew(true)
                .isActive(true)
                .endDate(LocalDateTime.now().toLocalDate().plusDays(subscriptionPlanEntity.getDurationDays()))
                .build();

        transactionService.processSubscriptionPayment(
                subscriptionRequest,
                subscriptionPlanEntity.getId(),
                subscriptionPlanEntity,
                "Payment for subscription");

        subscriptionRepository.save(subscriptionEntity);

        log.info("User {} subscribed to plan '{}'", user.getId(), subscriptionPlanEntity.getSubscriptionType());

        eventPublisher.publishEvent(
                new SubscribeEvent(user.getId()));
    }

    @Override
    public SubscriptionResponse getCurrentSubscription(Long userId) {

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        return subscriptionMapper.toDto(subscriptionEntity);
    }

    @Transactional
    @Override
    public void cancel(Long userId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);
        subscriptionEntity.setIsActive(false);
        subscriptionEntity.setAutoRenew(false);
        subscriptionEntity.setEndDate(LocalDate.now());

        log.info("User {} cancelled subscription '{}'", userId, subscriptionEntity.getSubscriptionPlan().getSubscriptionType());

        eventPublisher.publishEvent(
                new SubscriptionCancelledEvent(userId)
        );
    }

    @Override
    public boolean isActive(Long userId) {
        return subscriptionRepository
                .existsByUserIdAndIsActiveTrueAndEndDateAfter(
                        userId,
                        LocalDate.now()
                );
    }


    @Transactional
    @Override
    public void renew(Long userId, SubscriptionRequest subscriptionRequest) {

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(subscriptionRequest.getPlanId())
                .orElseThrow(() -> new NotFoundException("Subscription Plan Not Found"));

        Integer durationDays = subscriptionPlanEntity.getDurationDays();

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        transactionService.processSubscriptionPayment(
                subscriptionRequest,
                subscriptionEntity.getId(),
                subscriptionPlanEntity,
                "Payment for subscription renewal");

        LocalDate today = LocalDate.now();

        subscriptionEntity.setSubscriptionPlan(subscriptionPlanEntity);

        if (subscriptionEntity.getEndDate().isEqual(today) || subscriptionEntity.getEndDate().isAfter(today)) {
            subscriptionEntity.setEndDate(
                    subscriptionEntity.getEndDate().plusDays(durationDays)
            );
        } else {
            subscriptionEntity.setStartDate(today);
            subscriptionEntity.setEndDate(today.plusDays(durationDays));
            subscriptionEntity.setIsActive(true);
        }

        log.info("User {} renewed the subscription '{}'", userId, subscriptionEntity.getId());

        eventPublisher.publishEvent(
                new SubscriptionRenewedEvent(userId
                )
        );
    }

    @Transactional
    @Override
    public void enableAutoRenew(Long userId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        subscriptionEntity.setAutoRenew(true);

        log.info("Auto renew enabled for user '{}'", userId);

        eventPublisher.publishEvent(
                new AutoRenewEnabledEvent(userId));
    }

    @Transactional
    @Override
    public void disableAutoRenew(Long userId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        subscriptionEntity.setAutoRenew(false);

        log.info("Auto renew disabled for user '{}'", userId);

        eventPublisher.publishEvent(
                new AutoRenewDisableEvent(userId));
    }

    @Transactional
    @Override
    public void deactivateExpiredSubscriptions() {

        while (true) {

            Pageable pageable = PageRequest.of(0, 20);

            Page<SubscriptionEntity> result =
                    subscriptionRepository.findExpiredSubscriptions(
                            LocalDate.now(),
                            pageable
                    );

            if (result.isEmpty()) {
                break;
            }

            for (SubscriptionEntity subscription : result.getContent()) {
                subscription.setIsActive(false);
            }
        }

        log.info("Expired subscriptions deactivated");
    }
}
