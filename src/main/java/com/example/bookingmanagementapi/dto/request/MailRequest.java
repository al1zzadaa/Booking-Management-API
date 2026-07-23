package com.example.bookingmanagementapi.dto.request;

import lombok.Data;

@Data
public class MailRequest {
    private String to;
    private String subject;
    private String message;
}
