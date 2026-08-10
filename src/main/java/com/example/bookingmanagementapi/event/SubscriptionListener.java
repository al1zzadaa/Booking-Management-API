package com.example.bookingmanagementapi.event;

import com.example.bookingmanagementapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SubscriptionListener {

    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SubscribeEvent event) {
        System.out.println("Listener executed");
        notificationService.sendSubscribeNotification(event.userId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SubscriptionCancelledEvent event) {
        System.out.println("Listener cancelled");
        notificationService.sendSubscriptionCancellationNotification(event.userId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AutoRenewEnabledEvent event) {
        System.out.println("Listener cancelled");
        notificationService.sendEnableRenewalNotification(event.userId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AutoRenewDisableEvent event) {
        System.out.println("Listener cancelled");
        notificationService.sendDisableRenewalNotification(event.userId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SubscriptionRenewedEvent event) {
        notificationService.sendRenewalNotification(event.userId());
    }
}
