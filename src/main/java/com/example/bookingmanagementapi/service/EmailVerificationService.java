package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;

public interface EmailVerificationService {

    void verify(String token);

    void resend(String email);

    EmailVerificationTokenEntity create(UserEntity user);
}
