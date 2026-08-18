package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.MailRequest;
import com.example.bookingmanagementapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/text")
    public void sendTextEmail(@RequestBody MailRequest mailRequest) {
        emailService.sendTextEmail(mailRequest);
    }

    @PostMapping(value = "/attachment",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void sendWithAttachment(@RequestPart("mail") MailRequest mail,
                                   @RequestPart("file") MultipartFile file) {
        emailService.sendEmailWithAttachment(mail, file);
    }
}