package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.MailRequest;
import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.entity.NotificationEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.NotificationType;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.NotificationMapper;
import com.example.bookingmanagementapi.repository.NotificationRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.BookingService;
import com.example.bookingmanagementapi.service.EmailService;
import com.example.bookingmanagementapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Book;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;


    private NotificationRequest createNotification(
            NotificationType type,
            String title,
            String message) {

        return NotificationRequest.builder()
                .notificationType(type)
                .title(title)
                .message(message)
                .build();
    }

    private void notifyUser(
            Long userId,
            NotificationType type,
            String title,
            String message) {

        send(userId, createNotification(type, title, message));
    }

//    @Override
//    public void create(NotificationRequest notificationRequest) {
//        NotificationEntity notificationEntity = notificationMapper.toEntity(notificationRequest);
//
//        notificationRepository.save(notificationEntity);
//    }

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

//    @Override
//    public void update(Long id, UpdateNotificationRequest updateNotificationRequest) {
//
//        NotificationEntity notificationEntity = notificationRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Notification Not Found"));
//
//        notificationMapper.updateNotification(updateNotificationRequest,notificationEntity);
//
//        notificationRepository.save(notificationEntity);
//    }
//
//    @Override
//    public void delete(Long id) {
//
//        NotificationEntity notificationEntity = notificationRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("Notification Not Found"));
//
//        notificationRepository.deleteById(notificationEntity.getId());
//    }

    @Transactional
    @Override
    public void send(Long userId, NotificationRequest notificationRequest) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User Not Found"));


        NotificationEntity notification = notificationMapper.toEntity(notificationRequest);
        notification.setUser(user);
        notification.setRead(false);
        notification.setType(notificationRequest.getNotificationType());

        MailRequest mailRequest = new MailRequest();
        mailRequest.setTo(user.getEmail());
        mailRequest.setSubject(notificationRequest.getTitle());
        mailRequest.setMessage(notificationRequest.getMessage());

        emailService.sendTextEmail(mailRequest);

        notificationRepository.save(notification);
    }

    @Override
    public void markAsRead(Long notificationId) {
        NotificationEntity notificationEntity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification Not Found"));

        notificationEntity.setRead(true);

        notificationRepository.save(notificationEntity);
    }

    @Override
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {

        Page<NotificationEntity> notificationEntities = notificationRepository.findUnreadByUserId(userId, pageable);

        return notificationEntities.map(notificationMapper::toDto);
    }

    @Override
    public void sendBookingNotification(Long userId) {
        notifyUser(
                userId,
                NotificationType.BOOKING,
                "Booking",
                "Booking was successful"
        );
    }

    @Override
    public void sendSubscribeNotification(Long userId) {
        notifyUser(
                userId,
                NotificationType.SUBSCRIBE,
                "Subscription",
                "Subscription was successful"
        );
    }

    @Override
    public void sendTicketPaymentNotification(TicketEntity ticket) {
        notifyUser(
                ticket.getUser().getId(),
                NotificationType.PAYMENT,
                "Payment",
                "Payment was successful"
        );
    }

    @Override
    public void sendBookingPaymentNotification(Long userId) {
        notifyUser(
                userId,
                NotificationType.PAYMENT,
                "Payment",
                "Payment was successful"
        );
    }

    @Override
    public void sendTicketCancellationNotification(TicketEntity ticket) {
        notifyUser(ticket.getUser().getId(),
                NotificationType.CANCELLED,
                "Cancellation",
                "Cancellation was successful"
        );
    }

    @Override
    public void sendBookingCancellationNotification(Long userId) {
        notifyUser(userId,
                NotificationType.CANCELLED,
                "Cancellation",
                "Cancellation was successful"
        );
    }
}
