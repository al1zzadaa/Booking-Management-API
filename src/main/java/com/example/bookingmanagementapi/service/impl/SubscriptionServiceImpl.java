package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.*;
import com.example.bookingmanagementapi.exception.InsufficientBalanceException;
import com.example.bookingmanagementapi.mapper.SubscriptionMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import com.example.bookingmanagementapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

        transactionService.payForSubscription(subscriptionRequest);

        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                .user(user)
                .subscriptionPlan(subscriptionPlanEntity)
                .startDate(LocalDate.now())
                .endDate(LocalDateTime.now().toLocalDate()
                        .plusDays(subscriptionPlanEntity.getDurationDays()))
                .build();

        subscriptionRepository.save(subscriptionEntity);

        notificationService.sendSubscribeNotification(subscriptionRequest.getUserId());
    }

    @Override
    public SubscriptionResponse getCurrentSubscription(Long userId) {

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);

        return subscriptionMapper.toDto(subscriptionEntity);
    }

//    @Override
//    public void cancel(Long accountId) {
//        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(accountId, true);
//        subscriptionEntity.setIsActive(false);
//
//        NotificationRequest notificationRequest = new NotificationRequest();
//
//        notificationRequest.setNotificationType(NotificationType.CANCELLED);
//        notificationRequest.setTitle("Subscription notification");
//        notificationRequest.setMessage("Subscription canceled");
//
////        notificationService.create(notificationRequest);
//
//        subscriptionRepository.save(subscriptionEntity);
//    }
//
//    @Override
//    public boolean isActive(Long userId) {
//        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByUserIdAndIsActive(userId, true);
//        return subscriptionEntity.getEndDate().isAfter(LocalDate.now());
//    }
//
//
//
//    @Override
//    public void renew(Long accountId, Long planId) {
//
//        AccountEntity account = accountRepository.findById(accountId)
//                .orElseThrow(null);
//
//        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(planId)
//                .orElseThrow(null);
//
//
//        if (account.getBalance().compareTo(subscriptionPlanEntity.getPrice()) < 0) {
//            throw new InsufficientBalanceException("Not enough balance");
//        }
//
//
//        account.setBalance(account.getBalance().subtract(subscriptionPlanEntity.getPrice()));
//
//
//        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);
//
//
//        if (subscriptionEntity.getEndDate().isAfter(LocalDate.now())) {
//            subscriptionEntity.setEndDate(subscriptionEntity.getEndDate().plusMonths(1));
//        }else {
//            subscriptionEntity.setStartDate(LocalDate.now());
//            subscriptionEntity.setEndDate(LocalDate.now().plusMonths(1));
//        }
//
//        TransactionEntity tx = TransactionEntity.builder()
//                .type(TransactionType.PAYMENT)
//                .amount(subscriptionPlanEntity.getPrice())
//                .description("Subscription renewal")
//                .referenceId(subscriptionPlanEntity.getId())
//                .account(account)
//                .createdAt(LocalDateTime.now())
//                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
//                .referenceType(ReferenceType.SUBSCRIPTION)
//                .paymentStatus(PaymentStatus.PENDING)
//                .build();
//
//        transactionRepository.save(tx);
//
//
//        subscriptionEntity.setIsActive(true);
//        NotificationRequest notificationRequest = new NotificationRequest();
//
//        notificationRequest.setNotificationType(NotificationType.RENEW);
//        notificationRequest.setTitle("Subscription notification");
////        notificationRequest.setIsRead(false);
//        notificationRequest.setMessage("Subscription renewed");
//
////        notificationService.create(notificationRequest);
//
//        subscriptionRepository.save(subscriptionEntity);
//    }
//
//    @Override
//    public void autoRenew() {
//
//        Page<@NonNull SubscriptionEntity> result;
//
//        do {
//            Pageable pageable = PageRequest.of(page, size);
//
//            result = subscriptionRepository.findDueForRenewal(
//                    pageable
//            );
//
//            for (SubscriptionEntity sub : result.getContent()) {
//                try {
//                    renew(sub.getAccount().getId(), sub.getSubscriptionPlan().getId());
//                } catch (Exception e) {
//                    // log and continue
//                }
//            }
//
//            page++;
//
//        } while (result.hasNext());
//    }
//
//    @Override
//    public void enableAutoRenew(Long accountId) {
//        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);
//
//        subscriptionEntity.setAutoRenew(true);
//
//        NotificationRequest notificationRequest = new NotificationRequest();
//
//        notificationRequest.setNotificationType(NotificationType.RENEW);
//        notificationRequest.setTitle("Subscription notification");
////        notificationRequest.setIsRead(false);
//        notificationRequest.setMessage("Subscription auto renew enabled");
//
////        notificationService.create(notificationRequest);
//
//        subscriptionRepository.save(subscriptionEntity);
//    }
//
//    @Override
//    public void disableAutoRenew(Long accountId) {
//        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);
//
//        subscriptionEntity.setAutoRenew(false);
//
//        NotificationRequest notificationRequest = new NotificationRequest();
//
//        notificationRequest.setNotificationType(NotificationType.RENEW);
//        notificationRequest.setTitle("Subscription notification");
////        notificationRequest.setIsRead(false);
//        notificationRequest.setMessage("Subscription auto renew disabled");
//
////        notificationService.create(notificationRequest);
//
//        subscriptionRepository.save(subscriptionEntity);
//    }
}
