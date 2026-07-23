package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.request.UpdateNotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    void create(NotificationRequest notificationRequest);

    NotificationResponse getById(Long id);

    Page<NotificationResponse> getAll(Long userId, Pageable pageable);

    void update(Long id, UpdateNotificationRequest updateNotificationRequest);

    void delete(Long id);

    void send(Long accountId, NotificationRequest notificationRequest);

    void markAsRead(Long notificationId);

    Page<NotificationResponse> getUnreadNotifications(Long userId,  Pageable pageable);
}
