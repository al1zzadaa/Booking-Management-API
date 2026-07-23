package com.example.bookingmanagementapi.security;

import com.example.bookingmanagementapi.dto.request.LoginRequest;
import com.example.bookingmanagementapi.dto.request.RefreshRequest;
import com.example.bookingmanagementapi.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refreshToken(RefreshRequest refreshRequest);

    void logout(RefreshRequest refreshRequest);
}
