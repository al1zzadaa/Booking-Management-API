package com.example.bookingmanagementapi.scheduler;

import com.example.bookingmanagementapi.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTokenScheduler {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    //hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredTokens() {
        log.debug("Deleting expired email tokens");
        emailVerificationTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}
