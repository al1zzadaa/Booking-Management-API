package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import com.example.bookingmanagementapi.enums.PaymentMethods;
import com.example.bookingmanagementapi.repository.SubscriptionRepository;
import com.example.bookingmanagementapi.service.SubscriptionAutoRenewService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SubscriptionAutoRenewServiceImpl implements SubscriptionAutoRenewService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
//            (readOnly = true)
    public void autoRenew() {

        while (true) {

            Pageable pageable = PageRequest.of(0, 20);

            Page<SubscriptionEntity> result =
                    subscriptionRepository.findDueForRenewal(
                            pageable
                    );

            if (result.isEmpty()) {
                break;
            }

            for (SubscriptionEntity subscription : result.getContent()) {

                try {
                    SubscriptionRequest request =
                            SubscriptionRequest.builder()
                                    .userId(subscription.getUser().getId())
                                    .accountId(subscription.getAutoRenewAccount().getId())
                                    .planId(subscription.getSubscriptionPlan().getId())
                                    .build();

                    subscriptionService.renew(request);

                } catch (Exception e) {
//                    log.error(
//                            "Auto-renew failed for subscription {}",
//                            subscription.getId(),
//                            e
//                    );
                    System.out.println("Auto-renew failed for subscription {}");


                }
            }
        }
    }
}