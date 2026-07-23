package com.example.bookingmanagementapi.dto.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}