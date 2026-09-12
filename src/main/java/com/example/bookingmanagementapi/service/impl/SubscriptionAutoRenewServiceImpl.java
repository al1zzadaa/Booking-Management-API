package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.repository.SubscriptionRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.SubscriptionAutoRenewService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionAutoRenewServiceImpl implements SubscriptionAutoRenewService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    @Transactional
    public void autoRenew() {

        int renewedCount = 0;
        int failedCount = 0;

        while (true) {

            Pageable pageable = PageRequest.of(0, 20);

            Page<@NonNull SubscriptionEntity> result = subscriptionRepository.findDueForRenewal(pageable);

            if (result.isEmpty()) {
                break;
            }

            for (SubscriptionEntity subscription : result.getContent()) {

                try {
                    SubscriptionRequest request =
                            SubscriptionRequest.builder()
                                    .accountId(subscription.getAutoRenewAccount().getId())
                                    .planId(subscription.getSubscriptionPlan().getId())
                                    .build();

                    UserEntity userEntity = userRepository
                            .findByAccountsId(request.getAccountId())
                            .orElseThrow(() -> new NotFoundException("User not found"));

                    subscriptionService.renew(userEntity.getId(), request);

                    renewedCount++;

                    log.info(
                            "Auto-renewed subscription {} for user {}",
                            subscription.getId(),
                            userEntity.getId()
                    );

                } catch (Exception e) {

                    failedCount++;

                    log.error(
                            "Auto-renew failed for subscription {}",
                            subscription.getId(),
                            e
                    );
                }
            }
        }

        log.info(
                "Auto-renew job completed: {} renewed, {} failed",
                renewedCount,
                failedCount
        );

    }
}