package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.repository.RefreshTokenRepository
import com.example.bookingmanagementapi.service.impl.RefreshTokenCleanupServiceImpl
import spock.lang.Specification

import java.time.Instant

class RefreshTokenCleanupServiceTest extends Specification {

    def refreshTokenRepository = Mock(RefreshTokenRepository)

    def refreshTokenCleanupService = new RefreshTokenCleanupServiceImpl(
            refreshTokenRepository
    )

    def "deleteExpiredTokens should delete tokens expired before current time"() {
        when:
        refreshTokenCleanupService.deleteExpiredTokens()

        then:
        1 * refreshTokenRepository.deleteByExpiresAtBefore({
            Instant expiresAt ->
                expiresAt != null
        })
    }
}