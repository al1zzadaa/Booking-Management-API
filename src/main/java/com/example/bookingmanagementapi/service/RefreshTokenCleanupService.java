package com.example.bookingmanagementapi.service;

public interface RefreshTokenCleanupService {

    void deleteExpiredTokens();
}
