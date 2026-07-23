package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.MailRequest;
import org.springframework.web.multipart.MultipartFile;

public interface EmailService {

    void sendTextEmail(MailRequest request);

    void sendEmailWithAttachment(MailRequest request, MultipartFile file);
}
