package com.example.bookingmanagementapi.security;

import com.example.bookingmanagementapi.entity.RefreshTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-expiration-days}")
    private long refreshExpirationDays;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenEntity save(UserEntity user, String token) {

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenEntity findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));
    }

    @Transactional
    public void validate(RefreshTokenEntity refreshToken) {
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }
    }

    @Transactional
    public void delete(Long id) {
        refreshTokenRepository.deleteById(id);
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
