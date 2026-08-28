package com.example.bookingmanagementapi.scheduler;

import com.example.bookingmanagementapi.service.RefreshTokenCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenScheduler {

    private final RefreshTokenCleanupService cleanupService;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void deleteExpiredRefreshTokens() {
        cleanupService.deleteExpiredTokens();
    }
}
