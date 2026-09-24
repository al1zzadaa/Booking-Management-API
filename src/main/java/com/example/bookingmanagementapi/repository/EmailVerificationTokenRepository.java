package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<@NonNull EmailVerificationTokenEntity, @NonNull Long> {

    Optional<EmailVerificationTokenEntity> findByToken(String token);

    void deleteByUser(UserEntity user);

    void deleteAllByExpiresAtBefore(Instant now);
}