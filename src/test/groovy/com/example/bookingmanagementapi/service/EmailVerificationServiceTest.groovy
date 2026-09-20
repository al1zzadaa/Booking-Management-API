package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.MailRequest
import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.UserStatus
import com.example.bookingmanagementapi.exception.InvalidTokenException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.exception.TokenExpiredException
import com.example.bookingmanagementapi.repository.EmailVerificationTokenRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.EmailVerificationServiceImpl
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime

@Component
@RequiredArgsConstructor
class EmailVerificationServiceTest extends Specification {

    def verificationRepository = Mock(EmailVerificationTokenRepository)
    def userRepository = Mock(UserRepository)
    def emailService = Mock(EmailService)

    def emailVerificationService = new EmailVerificationServiceImpl(
            verificationRepository,
            userRepository,
            emailService
    )


    def "create should create verification token and send email successfully"() {
        given:
        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setIsActive(UserStatus.NOT_VERIFIED)

        def savedToken = new EmailVerificationTokenEntity()
        savedToken.setUser(user)
        savedToken.setToken("test-verification-token")
        savedToken.setCreatedAt(Instant.now())
        savedToken.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS))

        when:
        def result = emailVerificationService.create(user)

        then:
        1 * verificationRepository.deleteByUser(user)

        1 * verificationRepository.save(_) >> savedToken

        1 * emailService.sendTextEmail({
            MailRequest request ->
                request.to == "john@gmail.com"
                request.subject == "Email Verification"
                request.message.contains("test-verification-token")
                request.message.contains("expires in 24 hours")

                true
        })

        result == savedToken
    }


    def "create should delete existing token before creating new one"() {
        given:
        def user = new UserEntity()
        user.setEmail("john@gmail.com")

        def token = new EmailVerificationTokenEntity()

        when:
        def result = emailVerificationService.create(user)

        then:
        1 * verificationRepository.deleteByUser(user)

        1 * verificationRepository.save(_) >> token

        1 * emailService.sendTextEmail(_)

        result == token
    }


    def "verify should activate user and delete verification token successfully"() {
        given:
        def token = "valid-token"

        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setIsActive(UserStatus.NOT_VERIFIED)

        def verification = new EmailVerificationTokenEntity()
        verification.setToken(token)
        verification.setUser(user)
        verification.setCreatedAt(Instant.now().minusSeconds(60))
        verification.setExpiresAt(Instant.now().plusSeconds(3600))

        when:
        emailVerificationService.verify(token)

        then:
        1 * verificationRepository.findByToken(token) >> Optional.of(verification)

        1 * verificationRepository.delete(verification)

        user.getIsActive() == UserStatus.ACTIVE
        user.getEmailVerifiedAt() != null
        user.getEmailVerifiedAt() instanceof LocalDateTime
    }


    def "verify should throw InvalidTokenException when token does not exist"() {
        given:
        def token = "invalid-token"

        when:
        emailVerificationService.verify(token)

        then:
        1 * verificationRepository.findByToken(token) >> Optional.empty()

        0 * verificationRepository.delete(_)

        def exception = thrown(InvalidTokenException)
        exception.message == "Invalid verification token"
    }


    def "verify should throw TokenExpiredException when token is expired"() {
        given:
        def token = "expired-token"

        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setIsActive(UserStatus.NOT_VERIFIED)

        def verification = new EmailVerificationTokenEntity()
        verification.setToken(token)
        verification.setUser(user)
        verification.setCreatedAt(
                Instant.now().minus(25, ChronoUnit.HOURS)
        )
        verification.setExpiresAt(
                Instant.now().minus(1, ChronoUnit.HOURS)
        )

        when:
        emailVerificationService.verify(token)

        then:
        1 * verificationRepository.findByToken(token) >> Optional.of(verification)

        0 * verificationRepository.delete(_)

        def exception = thrown(TokenExpiredException)
        exception.message == "Verification token expired"

        user.getIsActive() == UserStatus.NOT_VERIFIED
        user.getEmailVerifiedAt() == null
    }


    def "resend should create new verification token successfully"() {
        given:
        def email = "john@gmail.com"

        def user = new UserEntity()
        user.setEmail(email)
        user.setIsActive(UserStatus.NOT_VERIFIED)

        def savedToken = new EmailVerificationTokenEntity()
        savedToken.setUser(user)
        savedToken.setToken("test-resend-token")
        savedToken.setCreatedAt(Instant.now())
        savedToken.setExpiresAt(
                Instant.now().plus(24, ChronoUnit.HOURS)
        )

        when:
        emailVerificationService.resend(email)

        then:
        1 * userRepository.findByEmail(email) >> Optional.of(user)

        1 * verificationRepository.deleteByUser(user)

        1 * verificationRepository.save(_) >> savedToken

        1 * emailService.sendTextEmail(_)
    }


    def "resend should throw NotFoundException when user does not exist"() {
        given:
        def email = "john@gmail.com"

        when:
        emailVerificationService.resend(email)

        then:
        1 * userRepository.findByEmail(email) >> Optional.empty()

        0 * verificationRepository._
        0 * emailService._

        def exception = thrown(NotFoundException)
        exception.message == "User not found"
    }


    def "resend should throw IllegalStateException when email is already verified"() {
        given:
        def email = "john@gmail.com"

        def user = new UserEntity()
        user.setEmail(email)
        user.setIsActive(UserStatus.ACTIVE)

        when:
        emailVerificationService.resend(email)

        then:
        1 * userRepository.findByEmail(email) >> Optional.of(user)

        0 * verificationRepository._
        0 * emailService._

        def exception = thrown(IllegalStateException)
        exception.message == "Email already verified"
    }
}