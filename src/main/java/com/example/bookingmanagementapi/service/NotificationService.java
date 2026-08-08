package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateNotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

//    void create(NotificationRequest notificationRequest);

    NotificationResponse getById(Long id);

    Page<NotificationResponse> getAll(Long userId, Pageable pageable);

//    void update(Long id, UpdateNotificationRequest updateNotificationRequest);
//
//    void delete(Long id);

    void send(Long userId, NotificationRequest notificationRequest);

    void markAsRead(Long notificationId);

    Page<NotificationResponse> getUnreadNotifications(Long userId,  Pageable pageable);

    void sendBookingNotification(Long userId);

    void sendSubscribeNotification(Long userId);

    void sendTicketPaymentNotification(TicketEntity ticket);

    void sendBookingPaymentNotification(Long userId);

    void sendTicketCancellationNotification(TicketEntity ticket);

    void sendBookingCancellationNotification(Long  userId);


}
