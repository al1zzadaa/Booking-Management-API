package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.MailRequest
import com.example.bookingmanagementapi.dto.request.NotificationRequest
import com.example.bookingmanagementapi.dto.response.NotificationResponse
import com.example.bookingmanagementapi.entity.NotificationEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.NotificationType
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.NotificationMapper
import com.example.bookingmanagementapi.repository.NotificationRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.NotificationServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import spock.lang.Specification

class NotificationServiceTest extends Specification {

    def notificationRepository = Mock(NotificationRepository)
    def notificationMapper = Mock(NotificationMapper)
    def emailService = Mock(EmailService)
    def userRepository = Mock(UserRepository)
    def validationUtil = Mock(ValidationUtil)

    def notificationService = new NotificationServiceImpl(
            notificationRepository,
            notificationMapper,
            emailService,
            userRepository,
            validationUtil
    )


    def "getById should return notification when user owns it"() {
        given:
        def user = new UserEntity()
        user.setId(10L)

        def notification = new NotificationEntity()
        notification.setId(1L)
        notification.setUser(user)

        def response = new NotificationResponse()

        when:
        def result = notificationService.getById(1L, 10L)

        then:
        1 * notificationRepository.findById(1L) >> Optional.of(notification)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(10L, 10L)
        1 * notificationMapper.toDto(notification) >> response

        result == response
    }


    def "getById should throw NotFoundException when notification does not exist"() {
        when:
        notificationService.getById(1L, 10L)

        then:
        1 * notificationRepository.findById(1L) >> Optional.empty()
        thrown(NotFoundException)

        0 * validationUtil._
        0 * notificationMapper._
    }


    def "getAll should return mapped notifications"() {
        given:
        def pageable = PageRequest.of(0, 10)

        def notification1 = new NotificationEntity()
        def notification2 = new NotificationEntity()

        def response1 = new NotificationResponse()
        def response2 = new NotificationResponse()

        def page = new PageImpl<NotificationEntity>(
                [notification1, notification2],
                pageable,
                2
        )

        when:
        def result = notificationService.getAll(10L, pageable)

        then:
        1 * notificationRepository.findAllByUserId(10L, pageable) >> page
        1 * notificationMapper.toDto(notification1) >> response1
        1 * notificationMapper.toDto(notification2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
    }


    def "getAll should return empty page when user has no notifications"() {
        given:
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<NotificationEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = notificationService.getAll(10L, pageable)

        then:
        1 * notificationRepository.findAllByUserId(10L, pageable) >> page
        0 * notificationMapper._

        result.empty
    }


    def "delete should delete notification when user owns it"() {
        given:
        def user = new UserEntity()
        user.setId(10L)

        def notification = new NotificationEntity()
        notification.setId(1L)
        notification.setUser(user)

        when:
        notificationService.delete(1L, 10L)

        then:
        1 * notificationRepository.findById(1L) >> Optional.of(notification)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(10L, 10L)
        1 * notificationRepository.deleteById(1L)
    }


    def "delete should throw NotFoundException when notification does not exist"() {
        when:
        notificationService.delete(1L, 10L)

        then:
        1 * notificationRepository.findById(1L) >> Optional.empty()
        thrown(NotFoundException)

        0 * validationUtil._
        0 * notificationRepository.deleteById(_)
    }


    def "send should create and save notification and send email"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        def request = NotificationRequest.builder()
                .notificationType(NotificationType.BOOKING)
                .title("Booking")
                .message("Booking was successful")
                .build()

        def notification = new NotificationEntity()

        when:
        notificationService.send(10L, request)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)

        1 * notificationMapper.toEntity(request) >> notification

        1 * emailService.sendTextEmail({
            MailRequest mail ->
                mail.getTo() == "test@gmail.com" &&
                        mail.getSubject() == "Booking" &&
                        mail.getMessage() == "Booking was successful"
        })

        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        !entity.getRead() &&
                        entity.getType() == NotificationType.BOOKING
        })
    }


    def "send should throw NotFoundException when user does not exist"() {
        given:
        def request = NotificationRequest.builder()
                .notificationType(NotificationType.BOOKING)
                .title("Booking")
                .message("Booking was successful")
                .build()

        when:
        notificationService.send(10L, request)

        then:
        1 * userRepository.findById(10L) >> Optional.empty()
        thrown(NotFoundException)

        0 * notificationMapper._
        0 * emailService._
        0 * notificationRepository.save(_)
    }


    def "markAsRead should mark notification as read"() {
        given:
        def user = new UserEntity()
        user.setId(10L)

        def notification = new NotificationEntity()
        notification.setId(1L)
        notification.setUser(user)
        notification.setRead(false)

        when:
        notificationService.markAsRead(1L, 10L)

        then:
        1 * notificationRepository.findById(1L) >> Optional.of(notification)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(10L, 10L)

        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getRead()
        })
    }


    def "markAsRead should throw NotFoundException when notification does not exist"() {
        when:
        notificationService.markAsRead(1L, 10L)

        then:
        1 * notificationRepository.findById(1L) >> Optional.empty()
        thrown(NotFoundException)

        0 * validationUtil._
        0 * notificationRepository.save(_)
    }


    def "getUnreadNotifications should return unread notifications"() {
        given:
        def pageable = PageRequest.of(0, 10)

        def notification1 = new NotificationEntity()
        def notification2 = new NotificationEntity()

        def response1 = new NotificationResponse()
        def response2 = new NotificationResponse()

        def page = new PageImpl<NotificationEntity>(
                [notification1, notification2],
                pageable,
                2
        )

        when:
        def result = notificationService.getUnreadNotifications(10L, pageable)

        then:
        1 * notificationRepository.findUnreadByUserId(10L, pageable) >> page
        1 * notificationMapper.toDto(notification1) >> response1
        1 * notificationMapper.toDto(notification2) >> response2

        result.content == [response1, response2]
    }


    def "getUnreadNotifications should return empty page"() {
        given:
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<NotificationEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = notificationService.getUnreadNotifications(10L, pageable)

        then:
        1 * notificationRepository.findUnreadByUserId(10L, pageable) >> page
        0 * notificationMapper._

        result.empty
    }


    def "sendBookingNotification should send BOOKING notification"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        when:
        notificationService.sendBookingNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.notificationType == NotificationType.BOOKING &&
                    it.title == "Booking" &&
                    it.message == "Booking was successful"
        }) >> new NotificationEntity()

        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save(_)
    }


    def "sendSubscribeNotification should send SUBSCRIBE notification"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        when:
        notificationService.sendSubscribeNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.notificationType == NotificationType.SUBSCRIBE &&
                    it.title == "Subscription" &&
                    it.message == "Subscription was successful"
        }) >> new NotificationEntity()

        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save(_)
    }


    def "sendTicketPaymentNotification should send PAYMENT notification"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        when:
        notificationService.sendTicketPaymentNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.notificationType == NotificationType.PAYMENT &&
                    it.title == "Payment" &&
                    it.message == "Ticket Payment was successful"
        }) >> new NotificationEntity()

        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save(_)
    }


    def "sendBookingPaymentNotification should send PAYMENT notification"() {

        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        when:
        notificationService.sendBookingPaymentNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.notificationType == NotificationType.PAYMENT &&
                    it.title == "Payment" &&
                    it.message == "Booking payment was successful"
        }) >> new NotificationEntity()

        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save(_)
    }


    def "sendTicketCancellationNotification should send CANCELLED notification"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        def notification = new NotificationEntity()

        when:
        notificationService.sendTicketCancellationNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)

        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.CANCELLED &&
                    it.getTitle() == "Cancellation" &&
                    it.getMessage() == "Ticket cancellation was successful"
        }) >> notification

        1 * emailService.sendTextEmail(_)

        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.CANCELLED
        })
    }

    def "sendBookingCancellationNotification should send CANCELLED notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendBookingCancellationNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.CANCELLED &&
                    it.getTitle() == "Cancellation" &&
                    it.getMessage() == "Booking cancellation was successful"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.CANCELLED
        })
    }


    def "sendSubscriptionCancellationNotification should send CANCELLED notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendSubscriptionCancellationNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.CANCELLED &&
                    it.getTitle() == "Cancellation" &&
                    it.getMessage() == "Subscription cancellation was successful"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.CANCELLED
        })
    }


    def "sendEnableRenewalNotification should send RENEW notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendEnableRenewalNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.RENEW &&
                    it.getTitle() == "Renewal" &&
                    it.getMessage() == "Subscription renew was enabled"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.RENEW
        })
    }


    def "sendDisableRenewalNotification should send RENEW notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendDisableRenewalNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.RENEW &&
                    it.getTitle() == "Renewal" &&
                    it.getMessage() == "Subscription renew was disabled"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.RENEW
        })
    }


    def "sendRenewalNotification should send RENEW notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendRenewalNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.RENEW &&
                    it.getTitle() == "Renewal" &&
                    it.getMessage() == "Subscription renewed successful"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.RENEW
        })
    }


    def "sendFlightExpirationNotification should send EXPIRE notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendFlightExpirationNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.EXPIRE &&
                    it.getTitle() == "Expire" &&
                    it.getMessage() == "Flight tickets expired"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.EXPIRE
        })
    }


    def "sendBookingExpirationNotification should send EXPIRE notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendBookingExpirationNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.EXPIRE &&
                    it.getTitle() == "Expire" &&
                    it.getMessage() == "Hotel bookings expired"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.EXPIRE
        })
    }


    def "sendApplyPromoCodeNotification should send PROMO_CODE notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.sendApplyPromoCodeNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.PROMO_CODE &&
                    it.getTitle() == "Promo Code" &&
                    it.getMessage() == "Promo Code Applied"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.PROMO_CODE
        })
    }


    def "passwordResetNotification should send PASSWORD_RESET notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.passwordResetNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.PASSWORD_RESET &&
                    it.getTitle() == "Password Reset" &&
                    it.getMessage() == "Password reset was successful"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.PASSWORD_RESET
        })
    }


    def "expireUnpaidNotification should send expiration notification"() {
        given:
        def user = new UserEntity(id: 10L, email: "test@gmail.com")
        def notification = new NotificationEntity()

        when:
        notificationService.expireUnpaidNotification(10L)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)
        1 * notificationMapper.toEntity({
            it.getNotificationType() == NotificationType.PROMO_CODE &&
                    it.getTitle() == "Expire" &&
                    it.getMessage() == "Expired due to time (15 minutes)"
        }) >> notification
        1 * emailService.sendTextEmail(_)
        1 * notificationRepository.save({
            NotificationEntity entity ->
                entity == notification &&
                        entity.getUser() == user &&
                        entity.getRead() == false &&
                        entity.getType() == NotificationType.PROMO_CODE
        })
    }
}