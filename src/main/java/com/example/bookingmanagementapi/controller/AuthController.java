package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.ForgotPasswordRequest;
import com.example.bookingmanagementapi.dto.request.LoginRequest;
import com.example.bookingmanagementapi.dto.request.ResetPasswordRequest;
import com.example.bookingmanagementapi.service.PasswordResetService;
import com.example.bookingmanagementapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {


    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public void login(LoginRequest loginRequest){}


    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

}
