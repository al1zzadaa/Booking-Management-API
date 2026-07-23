package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.request.UpdateNotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.NotificationEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.NotificationMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.repository.NotificationRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    @Override
    public void create(NotificationRequest notificationRequest) {
        NotificationEntity notificationEntity = notificationMapper.toEntity(notificationRequest);

        notificationRepository.save(notificationEntity);
    }

    @Override
    public NotificationResponse getById(Long id) {
        NotificationEntity notificationEntity = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification Not Found"));

        return notificationMapper.toDto(notificationEntity);
    }

    @Override
    public Page<NotificationResponse> getAll(Long userId, Pageable pageable) {

        Page<NotificationEntity> notificationEntities = notificationRepository.findAllByUserId(userId, pageable);

        return notificationEntities.map(notificationMapper::toDto);
    }

    @Override
    public void update(Long id, UpdateNotificationRequest updateNotificationRequest) {

        NotificationEntity notificationEntity = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification Not Found"));

        notificationMapper.updateNotification(updateNotificationRequest,notificationEntity);

        notificationRepository.save(notificationEntity);
    }

    @Override
    public void delete(Long id) {

        NotificationEntity notificationEntity = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification Not Found"));

        notificationRepository.deleteById(notificationEntity.getId());
    }

    @Override
    public void send(Long accountId, NotificationRequest notificationRequest) {

        UserEntity user = userRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("User Not Found"));


        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setTitle(notificationRequest.getTitle());
        notification.setMessage(notificationRequest.getMessage());
        notification.setRead(false);
        notification.setType(notificationRequest.getNotificationType());

        notificationRepository.save(notification);

    }

    @Override
    public void markAsRead(Long notificationId) {
        NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification Not Found"));

        notificationEntity.setRead(true);
    }

    @Override
    public Page<NotificationResponse> getUnreadNotifications(Long userId,  Pageable pageable) {

        Page<NotificationEntity> notificationEntities = notificationRepository.findUnreadByUserId(userId, pageable);

        return notificationEntities.map(notificationMapper::toDto);
    }
}
