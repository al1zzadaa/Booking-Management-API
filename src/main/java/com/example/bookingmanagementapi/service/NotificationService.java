package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    NotificationResponse getById(Long id, Long userId);

    Page<@NonNull NotificationResponse> getAll(Long userId, Pageable pageable);

    void delete(Long notificationId, Long userId);

    void send(Long userId, NotificationRequest notificationRequest);

    void markAsRead(Long notificationId, Long userId);

    Page<@NonNull NotificationResponse> getUnreadNotifications(Long userId,  Pageable pageable);

    void sendBookingNotification(Long userId);

    void sendSubscribeNotification(Long userId);

    void sendTicketPaymentNotification(Long userId);

    void sendBookingPaymentNotification(Long userId);

    void sendTicketCancellationNotification(Long userId);

    void sendBookingCancellationNotification(Long  userId);

    void sendSubscriptionCancellationNotification(Long  userId);

    void sendEnableRenewalNotification(Long  userId);

    void sendDisableRenewalNotification(Long  userId);

    void sendRenewalNotification(Long  userId);

    void sendFlightExpirationNotification(Long  userId);

    void sendBookingExpirationNotification(Long  userId);

    void sendApplyPromoCodeNotification(Long  userId);

    void expireUnpaidNotification(Long userId);

    void passwordResetNotification(Long userId);
}
