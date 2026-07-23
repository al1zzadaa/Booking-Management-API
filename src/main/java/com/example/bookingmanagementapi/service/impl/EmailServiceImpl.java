package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.MailRequest;
import com.example.bookingmanagementapi.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;


    public void sendTextEmail(MailRequest request) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getMessage());

        mailSender.send(message);
    }

    public void sendEmailWithAttachment(MailRequest request,  MultipartFile file) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(from);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getMessage());

//            FileSystemResource file =
//                    new FileSystemResource(file);

//            helper.addAttachment(file.getFilename(), file);
            helper.addAttachment(
                    Objects.requireNonNull(file.getOriginalFilename()),
                    new ByteArrayResource(file.getBytes()));

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}