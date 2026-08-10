package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.event.AutoRenewEnabledEvent;
import com.example.bookingmanagementapi.event.SubscribeEvent;
import com.example.bookingmanagementapi.event.SubscriptionCancelledEvent;
import com.example.bookingmanagementapi.event.SubscriptionRenewedEvent;
import com.example.bookingmanagementapi.exception.InsufficientBalanceException;
import com.example.bookingmanagementapi.mapper.SubscriptionMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import com.example.bookingmanagementapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${subscription.size}")
    private int size;
    @Value("${subscription.page}")
    private int page;


    @Transactional
    @Override
    public void subscribe(SubscriptionRequest subscriptionRequest) {

        System.out.println(subscriptionRequest.getUserId());

        UserEntity user = userRepository.findById(subscriptionRequest.getUserId()).orElseThrow(null);

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findByIdAndActive(subscriptionRequest.getPlanId(), true);

        AccountEntity account = accountRepository.findById(subscriptionRequest.getAccountId()).orElseThrow(null);


        if (subscriptionPlanEntity.getPrice().compareTo(account.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        transactionService.payForSubscription(subscriptionRequest, subscriptionPlanEntity);

        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                .user(user)
                .subscriptionPlan(subscriptionPlanEntity)
                .startDate(LocalDate.now())
                .autoRenewAccount(account)
                .endDate(LocalDateTime.now().toLocalDate()
                        .plusDays(subscriptionPlanEntity.getDurationDays()))
                .build();

        subscriptionRepository.save(subscriptionEntity);

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

        eventPublisher.publishEvent(
                new SubscriptionCancelledEvent(userId)
        );
    }

    //
    @Override
    public boolean isActive(Long userId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);
        return subscriptionEntity.getEndDate().isAfter(LocalDate.now());
    }


    @Transactional
    @Override
    public void renew(SubscriptionRequest subscriptionRequest) {

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(subscriptionRequest.getPlanId())
                .orElseThrow(null);

        Integer durationDays = subscriptionPlanEntity.getDurationDays();

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(subscriptionRequest.getUserId(), true);

        transactionService.subscriptionRenew(subscriptionRequest, subscriptionEntity.getId(), subscriptionPlanEntity);
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

        eventPublisher.publishEvent(
                new SubscriptionRenewedEvent(subscriptionRequest.getUserId()
                )
        );
    }

//    @Override
//    public void autoRenew() {
//        while (true) {
//            Pageable pageable = PageRequest.of(0, size);
//            Page<SubscriptionEntity> result = subscriptionRepository.findDueForRenewal(pageable);
//            if (result.isEmpty()) {
//                break;
//            }
//            for (SubscriptionEntity subscription : result.getContent()) {
//                try {
//                    SubscriptionRequest request = SubscriptionRequest.builder().userId(subscription.getUser().getId()).accountId(subscription.getAutoRenewAccount().getId()).planId(subscription.getSubscriptionPlan().getId()).paymentMethod(PaymentMethods.ACCOUNT_BALANCE).build();
//                    renew(request);
//                } catch (Exception e){
//                // Log the failure and continue with the next subscription
////                 log.error( "Auto-renew failed for subscription {}", subscription.getId(), e );
//                 }
//            }
//        }
//    }

    @Transactional
    @Override
    public void enableAutoRenew(Long userId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        subscriptionEntity.setAutoRenew(true);

        eventPublisher.publishEvent(
                new AutoRenewEnabledEvent(userId));
    }

    @Transactional
    @Override
    public void disableAutoRenew(Long userId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        subscriptionEntity.setAutoRenew(false);

        eventPublisher.publishEvent(
                new AutoRenewEnabledEvent(userId));
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
    }
}
