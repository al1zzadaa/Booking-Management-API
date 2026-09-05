package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MailWithAttachmentRequest extends MailRequest{

    @NotBlank
    @Size(min = 1, max = 2000)
    private String path;
}
