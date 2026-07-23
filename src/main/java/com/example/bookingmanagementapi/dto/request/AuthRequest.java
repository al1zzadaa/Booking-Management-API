package com.example.bookingmanagementapi.dto.request;

import java.time.LocalDate;

public class AuthRequest {
    private String name;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private LocalDate birthDate;
}
