package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.*;
import com.example.bookingmanagementapi.exception.InsufficientBalanceException;
import com.example.bookingmanagementapi.exception.SubscriptionException;
import com.example.bookingmanagementapi.mapper.SubscriptionMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.SubscriptionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    @Value("${subscription.size}")
    private int size;
    @Value("${subscription.page}")
    private int page;


    @Override
    public void subscribe(Long userId, Long planId, PaymentRequest paymentRequest) {

        UserEntity account = userRepository.findById(userId).orElseThrow(null);

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(planId)
                .orElseThrow(null);

        SubscriptionEntity subscription =
                subscriptionRepository
                        .findBySubscriptionPlanAndIsActive(subscriptionPlanEntity, true);

        if (!subscription.getStartDate().isBefore(LocalDate.now().minusDays(3))) {
            throw new SubscriptionException("You can change subscription after 3 days");
        }

//        if (paymentRequest.getAccountId().compareTo(subscriptionPlanEntity.getPrice()) < 0) {
//            throw new InsufficientBalanceException("Not enough balance");
//        }
//
//        BigDecimal price = account.getBalance();
//
//        if (account.getCurrency().equals(Currency.USD)){
//            price = account.getBalance().multiply(BigDecimal.valueOf(17)).divide(BigDecimal.valueOf(10));
//        }else if (account.getCurrency().equals(Currency.EUR)){
//            price = account.getBalance().multiply(BigDecimal.valueOf(2));
//        }
//
//        account.setBalance(
//               price.subtract(subscriptionPlanEntity.getPrice())
//        );
//
//
//
//        TransactionEntity tx = TransactionEntity.builder()
//                .type(TransactionType.PAYMENT)
//                .amount(subscriptionPlanEntity.getPrice())
//                .description("Payment for subscription")
//                .referenceId(subscriptionPlanEntity.getId())
//                .account(account)
//                .createdAt(LocalDateTime.now())
//                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
//                .referenceType(ReferenceType.SUBSCRIPTION)
//                .paymentStatus(PaymentStatus.PENDING)
//                .build();
//        tx.setAccount(userEntity.getAccount());
//        tx.setAmount(subscriptionPlanEntity.getPrice());
//        tx.setDescription("Subscribed to subscription");
//
//        transactionRepository.save(tx);


//        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);
//        subscriptionEntity.setAccount(account);
//        subscriptionEntity.setSubscriptionPlan(subscriptionPlanEntity);
//        subscriptionEntity.setStartDate(LocalDateTime.now().toLocalDate());
//        subscriptionEntity.setEndDate(
//                LocalDateTime.now().toLocalDate()
//                        .plusDays(subscriptionPlanEntity.getDurationDays())
//        );
//        subscriptionEntity.setIsActive(true);
//
//        accountRepository.save(account);
//        subscriptionRepository.save(subscriptionEntity);
//
//
        //TODO method
//        NotificationRequest notificationRequest = new NotificationRequest();
//
//        notificationRequest.setNotificationType(NotificationType.SUBSCRIBE);
//        notificationRequest.setTitle("Subscription notification");
//        notificationRequest.setIsRead(false);
//        notificationRequest.setMessage("Subscription activated");
//
//        notificationService.create(notificationRequest);
//
//        notificationService.send(, notificationRequest);
    }

    @Override
    public SubscriptionResponse getCurrentSubscription(Long accountId) {

        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);

        return subscriptionMapper.toDto(subscriptionEntity);
    }

    @Override
    public void cancel(Long accountId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);
        subscriptionEntity.setIsActive(false);

        NotificationRequest notificationRequest = new NotificationRequest();

        notificationRequest.setNotificationType(NotificationType.CANCELLED);
        notificationRequest.setTitle("Subscription notification");
        notificationRequest.setIsRead(false);
        notificationRequest.setMessage("Subscription canceled");

//        notificationService.create(notificationRequest);

        subscriptionRepository.save(subscriptionEntity);
    }

    @Override
    public boolean isActive(Long accountId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);
        return subscriptionEntity.getEndDate().isAfter(LocalDate.now());
    }



    @Override
    public void renew(Long accountId, Long planId) {

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(null);

        SubscriptionPlanEntity subscriptionPlanEntity = subscriptionPlanRepository.findById(planId)
                .orElseThrow(null);


        if (account.getBalance().compareTo(subscriptionPlanEntity.getPrice()) < 0) {
            throw new InsufficientBalanceException("Not enough balance");
        }


        account.setBalance(account.getBalance().subtract(subscriptionPlanEntity.getPrice()));


        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);


        if (subscriptionEntity.getEndDate().isAfter(LocalDate.now())) {
            subscriptionEntity.setEndDate(subscriptionEntity.getEndDate().plusMonths(1));
        }else {
            subscriptionEntity.setStartDate(LocalDate.now());
            subscriptionEntity.setEndDate(LocalDate.now().plusMonths(1));
        }

        TransactionEntity tx = TransactionEntity.builder()
                .type(TransactionType.PAYMENT)
                .amount(subscriptionPlanEntity.getPrice())
                .description("Subscription renewal")
                .referenceId(subscriptionPlanEntity.getId())
                .account(account)
                .createdAt(LocalDateTime.now())
                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
                .referenceType(ReferenceType.SUBSCRIPTION)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        transactionRepository.save(tx);


        subscriptionEntity.setIsActive(true);
        NotificationRequest notificationRequest = new NotificationRequest();

        notificationRequest.setNotificationType(NotificationType.RENEW);
        notificationRequest.setTitle("Subscription notification");
        notificationRequest.setIsRead(false);
        notificationRequest.setMessage("Subscription renewed");

//        notificationService.create(notificationRequest);

        subscriptionRepository.save(subscriptionEntity);
    }

    @Override
    public void autoRenew() {

        Page<@NonNull SubscriptionEntity> result;

        do {
            Pageable pageable = PageRequest.of(page, size);

            result = subscriptionRepository.findDueForRenewal(
                    pageable
            );

            for (SubscriptionEntity sub : result.getContent()) {
                try {
                    renew(sub.getAccount().getId(), sub.getSubscriptionPlan().getId());
                } catch (Exception e) {
                    // log and continue
                }
            }

            page++;

        } while (result.hasNext());
    }

    @Override
    public void enableAutoRenew(Long accountId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);

        subscriptionEntity.setAutoRenew(true);

        NotificationRequest notificationRequest = new NotificationRequest();

        notificationRequest.setNotificationType(NotificationType.RENEW);
        notificationRequest.setTitle("Subscription notification");
        notificationRequest.setIsRead(false);
        notificationRequest.setMessage("Subscription auto renew enabled");

//        notificationService.create(notificationRequest);

        subscriptionRepository.save(subscriptionEntity);
    }

    @Override
    public void disableAutoRenew(Long accountId) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findByAccountId(accountId);

        subscriptionEntity.setAutoRenew(false);

        NotificationRequest notificationRequest = new NotificationRequest();

        notificationRequest.setNotificationType(NotificationType.RENEW);
        notificationRequest.setTitle("Subscription notification");
        notificationRequest.setIsRead(false);
        notificationRequest.setMessage("Subscription auto renew disabled");

//        notificationService.create(notificationRequest);

        subscriptionRepository.save(subscriptionEntity);
    }
}
