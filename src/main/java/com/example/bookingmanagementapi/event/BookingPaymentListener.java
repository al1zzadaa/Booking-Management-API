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
public class BookingPaymentListener {

    private final NotificationService notificationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BookingPaymentEvent event) {
        System.out.println("Listener executed");
        notificationService.sendBookingPaymentNotification(event.userId());

    }
}