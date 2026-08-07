package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.MailRequest;
import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.UserStatus;
import com.example.bookingmanagementapi.exception.InvalidTokenException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.TokenExpiredException;
import com.example.bookingmanagementapi.repository.EmailVerificationTokenRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.EmailService;
import com.example.bookingmanagementapi.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;


    @Transactional
    public EmailVerificationTokenEntity create(UserEntity user) {

        // remove old token if exists
        verificationRepository.deleteByUser(user);

        EmailVerificationTokenEntity verification =
                new EmailVerificationTokenEntity();

        verification.setUser(user);
        verification.setToken(generateToken());
        verification.setCreatedAt(Instant.now());
        verification.setExpiresAt(
                Instant.now().plus(24, ChronoUnit.HOURS)
        );

        EmailVerificationTokenEntity saved =
                verificationRepository.save(verification);


        sendVerificationEmail(user, saved.getToken());

        return saved;
    }


    @Transactional
    public void verify(String token) {

        EmailVerificationTokenEntity verification =
                verificationRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Invalid verification token"
                                ));



        if (verification.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new TokenExpiredException(
                    "Verification token expired"
            );
        }


        UserEntity user = verification.getUser();


//        if (verification.getTokenType() == TokenType.EMAIL_VERIFICATION) {
//            user.setIsActive(UserStatus.ACTIVE);
//            user.setEmailVerifiedAt(LocalDateTime.now());
//            verificationRepository.delete(verification);
//        }
//        else if (verification.getTokenType() == TokenType.PASSWORD_RESET) {
//            // allow password reset
//        }
        user.setIsActive(UserStatus.ACTIVE);
        user.setEmailVerifiedAt(LocalDateTime.now());


        verificationRepository.delete(verification);
    }


    @Transactional
    public void resend(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found"
                        ));


        if (user.getIsActive().equals(UserStatus.ACTIVE)) {
            throw new IllegalStateException(
                    "Email already verified"
            );
        }


        create(user);
    }


    private String generateToken() {
        return UUID.randomUUID().toString();
    }


    private void sendVerificationEmail(UserEntity user, String token) {

        String link =
                "http://localhost:8080/email-verification?token="
                        + token;

        MailRequest  mailRequest = new MailRequest();
        mailRequest.setTo(user.getEmail());
        mailRequest.setSubject("Email Verification");
        mailRequest.setMessage("""
                Hello %s,

                Please verify your email by clicking this link:

                %s

                This link expires in 24 hours.
                """.formatted(user.getEmail(), link));

        emailService.sendTextEmail(mailRequest);
    }

}