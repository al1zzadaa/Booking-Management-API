package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.ForgotPasswordRequest
import com.example.bookingmanagementapi.dto.request.ResetPasswordRequest
import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.TokenType
import com.example.bookingmanagementapi.exception.InvalidTokenException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.exception.TokenExpiredException
import com.example.bookingmanagementapi.repository.EmailVerificationTokenRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.PasswordResetServiceImpl
import lombok.RequiredArgsConstructor
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import spock.lang.Specification

import java.time.Instant

class PasswordResetServiceTest extends Specification {

    def userRepository = Mock(UserRepository)
    def emailVerificationTokenRepository = Mock(EmailVerificationTokenRepository)
    def passwordEncoder = Mock(PasswordEncoder)
    def emailService = Mock(EmailService)
    def applicationEventPublisher = Mock(ApplicationEventPublisher)

    def passwordResetService = new PasswordResetServiceImpl(
            userRepository,
            emailVerificationTokenRepository,
            passwordEncoder,
            emailService,
            applicationEventPublisher
    )


    def "resetPassword should reset password successfully"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")

        def verification = new EmailVerificationTokenEntity()
        verification.setToken("valid-token")
        verification.setTokenType(TokenType.PASSWORD_RESET)
        verification.setExpiresAt(Instant.now().plusSeconds(900))
        verification.setUser(user)

        def request = new ResetPasswordRequest()
        request.setToken("valid-token")
        request.setNewPassword("newPassword123")

        when:
        passwordResetService.resetPassword(request)

        then:
        1 * emailVerificationTokenRepository.findByToken("valid-token") >> Optional.of(verification)
        1 * passwordEncoder.encode("newPassword123") >> "encoded-password"
        1 * applicationEventPublisher.publishEvent({
            it.userId == 10L
        })
        1 * emailVerificationTokenRepository.delete(verification)

        user.getPassword() == "encoded-password"
    }


    def "resetPassword should throw InvalidTokenException when token does not exist"() {
        given:
        def request = new ResetPasswordRequest()
        request.setToken("invalid-token")
        request.setNewPassword("newPassword123")

        when:
        passwordResetService.resetPassword(request)

        then:
        1 * emailVerificationTokenRepository.findByToken("invalid-token") >> Optional.empty()

        thrown(InvalidTokenException)

        0 * passwordEncoder._
        0 * applicationEventPublisher._
        0 * emailVerificationTokenRepository.delete(_)
    }


    def "resetPassword should throw InvalidTokenException when token type is incorrect"() {
        given:
        def verification = new EmailVerificationTokenEntity()
        verification.setToken("valid-token")
        verification.setTokenType(TokenType.EMAIL_VERIFICATION)
        verification.setExpiresAt(Instant.now().plusSeconds(900))

        def request = new ResetPasswordRequest()
        request.setToken("valid-token")
        request.setNewPassword("newPassword123")

        when:
        passwordResetService.resetPassword(request)

        then:
        1 * emailVerificationTokenRepository.findByToken("valid-token") >> Optional.of(verification)

        thrown(InvalidTokenException)

        0 * passwordEncoder._
        0 * applicationEventPublisher._
        0 * emailVerificationTokenRepository.delete(_)
    }


    def "resetPassword should throw TokenExpiredException when token is expired"() {
        given:
        def verification = new EmailVerificationTokenEntity()
        verification.setToken("expired-token")
        verification.setTokenType(TokenType.PASSWORD_RESET)
        verification.setExpiresAt(Instant.now().minusSeconds(60))

        def request = new ResetPasswordRequest()
        request.setToken("expired-token")
        request.setNewPassword("newPassword123")

        when:
        passwordResetService.resetPassword(request)

        then:
        1 * emailVerificationTokenRepository.findByToken("expired-token") >> Optional.of(verification)

        thrown(TokenExpiredException)

        0 * passwordEncoder._
        0 * applicationEventPublisher._
        0 * emailVerificationTokenRepository.delete(_)
    }


    def "forgotPassword should create password reset token and send email"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("test@gmail.com")
        user.setFirstName("John")

        def request = new ForgotPasswordRequest()
        request.setEmail("test@gmail.com")

        when:
        passwordResetService.forgotPassword(request)

        then:
        1 * userRepository.findByEmail("test@gmail.com") >> Optional.of(user)
        1 * emailVerificationTokenRepository.deleteByUser(user)

        1 * emailVerificationTokenRepository.save({
            EmailVerificationTokenEntity token ->
                token.getUser() == user &&
                        token.getToken() != null &&
                        token.getCreatedAt() != null &&
                        token.getExpiresAt().isAfter(token.getCreatedAt()) &&
                        token.getExpiresAt().isBefore(
                                token.getCreatedAt().plusSeconds(16 * 60)
                        ) &&
                        token.getTokenType() == TokenType.PASSWORD_RESET
        }) >> { EmailVerificationTokenEntity token ->
            token
        }

        1 * emailService.sendTextEmail({
            it.getTo() == "test@gmail.com" &&
                    it.getSubject() == "Reset Password" &&
                    it.getMessage().contains("John") &&
                    it.getMessage().contains("reset your password") &&
                    it.getMessage().contains("15 minutes")
        })
    }


    def "forgotPassword should throw NotFoundException when user does not exist"() {
        given:
        def request = new ForgotPasswordRequest()
        request.setEmail("unknown@gmail.com")

        when:
        passwordResetService.forgotPassword(request)

        then:
        1 * userRepository.findByEmail("unknown@gmail.com") >> Optional.empty()

        thrown(NotFoundException)

        0 * emailVerificationTokenRepository._
        0 * emailService._
    }
}