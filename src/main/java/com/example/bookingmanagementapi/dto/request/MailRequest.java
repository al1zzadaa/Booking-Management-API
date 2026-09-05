package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MailRequest {

    @NotBlank
    @Email
    private String to;

    @NotBlank
    @Size(min = 1, max = 200)
    private String subject;

    @NotBlank
    @Size(min = 1, max = 2000)
    private String message;
}
