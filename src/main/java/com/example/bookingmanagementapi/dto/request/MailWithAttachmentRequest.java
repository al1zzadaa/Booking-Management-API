package com.example.bookingmanagementapi.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MailWithAttachmentRequest extends MailRequest{
    private String path;
}
