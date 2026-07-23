package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping
    public String verifyEmail(@RequestParam String token) {
        emailVerificationService.verify(token);
        return "email verified";
    }

    @PostMapping("/resend")
    public String resend(@RequestParam String email) {
        emailVerificationService.resend(email);
        return "Verification email sent again";
    }
}