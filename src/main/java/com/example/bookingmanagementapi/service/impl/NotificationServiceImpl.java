package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.MailRequest;
import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.entity.NotificationEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.NotificationType;
import com.example.bookingmanagementapi.exception.EmailSendingError;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.NotificationMapper;
import com.example.bookingmanagementapi.repository.NotificationRepository;
import com.example.bookingmanagementapi.repository.TicketRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.EmailService;
import com.example.bookingmanagementapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

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

    @Override
    public void send(Long userId, NotificationRequest notificationRequest) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User Not Found"));


        NotificationEntity notification = new NotificationEntity();
        notification.setUser(user);
        notification.setTitle(notificationRequest.getTitle());
        notification.setMessage(notificationRequest.getMessage());
        notification.setRead(false);
        notification.setType(notificationRequest.getNotificationType());

        MailRequest mailRequest = new MailRequest();
        mailRequest.setTo(user.getEmail());
        mailRequest.setSubject(notificationRequest.getTitle());
        mailRequest.setMessage(notificationRequest.getMessage());

        try{
            emailService.sendTextEmail(mailRequest);

        }catch(EmailSendingError e){
            e.printStackTrace();
            throw new  EmailSendingError("Email SendingError");
        }

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
    public Page<NotificationResponse> getUnreadNotifications(Long userId,  Pageable pageable) {

        Page<NotificationEntity> notificationEntities = notificationRepository.findUnreadByUserId(userId, pageable);

        return notificationEntities.map(notificationMapper::toDto);
    }

    @Override
    public void sendBookingNotification(Long userId){
        NotificationRequest notification = NotificationRequest.builder()
                .notificationType(NotificationType.BOOKING)
                .title("Booking")
                .isRead(false)
                .message("Booking is successful")
                .build();

        send(userId, notification);
    }

    @Override
    public void sendTicketPaymentNotification(TicketEntity ticket) {
        NotificationRequest notification = NotificationRequest.builder()
                .notificationType(NotificationType.PAYMENT)
                .title("Payment")
                .isRead(false)
                .message("Payment was successful")
                .build();

        send(ticket.getUser().getId(), notification);
    }

    @Override
    public void sendCancellationNotification(TicketEntity ticket) {
        NotificationRequest notification = NotificationRequest.builder()
                .notificationType(NotificationType.CANCELLED)
                .title("Cancellation")
                .isRead(false)
                .message("Cancellation was successful")
                .build();

        send(ticket.getUser().getId(), notification);
    }
}
