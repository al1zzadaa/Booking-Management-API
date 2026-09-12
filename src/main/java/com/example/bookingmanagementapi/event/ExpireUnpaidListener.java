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
public class ExpireUnpaidListener {

    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ExpireUnpaidEvent event) {
        notificationService.sendApplyPromoCodeNotification(event.userId());
    }
}
