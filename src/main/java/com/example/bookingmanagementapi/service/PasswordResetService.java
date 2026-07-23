package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.ForgotPasswordRequest;
import com.example.bookingmanagementapi.dto.request.ResetPasswordRequest;

public interface PasswordResetService {

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
