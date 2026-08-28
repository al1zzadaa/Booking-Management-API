package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByToken(String token);

    void deleteAllByUser_Id(Long id);

    void deleteByExpiresAtBefore(Instant expiresAt);
}
