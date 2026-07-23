package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.ForgotPasswordRequest;
import com.example.bookingmanagementapi.dto.request.MailRequest;
import com.example.bookingmanagementapi.dto.request.ResetPasswordRequest;
import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.TokenType;
import com.example.bookingmanagementapi.exception.InvalidTokenException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.TokenExpiredException;
import com.example.bookingmanagementapi.repository.EmailVerificationTokenRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.EmailService;
import com.example.bookingmanagementapi.service.EmailVerificationService;
import com.example.bookingmanagementapi.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        EmailVerificationTokenEntity verification =
                emailVerificationTokenRepository.findByToken(request.getToken())
                        .orElseThrow(() ->
                                new InvalidTokenException("Invalid token"));

        if (verification.getTokenType() != TokenType.PASSWORD_RESET) {
            throw new InvalidTokenException("Invalid token type");
        }

        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token expired");
        }

        UserEntity user = verification.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        emailVerificationTokenRepository.delete(verification);
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {

        UserEntity user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        emailVerificationTokenRepository.deleteByUser(user);

        EmailVerificationTokenEntity verification =
                new EmailVerificationTokenEntity();

        verification.setUser(user);
        verification.setToken(generateToken());
        verification.setCreatedAt(Instant.now());
        verification.setExpiresAt(
                Instant.now().plus(15, ChronoUnit.MINUTES)
        );
        verification.setTokenType(TokenType.PASSWORD_RESET);

        emailVerificationTokenRepository.save(verification);

        sendPasswordResetEmail(user, verification.getToken());
    }


    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private void sendPasswordResetEmail(UserEntity user, String token) {

        String link =
                "http://localhost:8080/auth/reset-password?token=" + token;

        MailRequest mailRequest = new MailRequest();

        mailRequest.setTo(user.getEmail());
        mailRequest.setSubject("Reset Password");

        mailRequest.setMessage("""
            Hello %s,

            Click the link below to reset your password:

            %s

            This link expires in 15 minutes.
            """.formatted(user.getFirstName(), link));

        emailService.sendTextEmail(mailRequest);
    }
}
