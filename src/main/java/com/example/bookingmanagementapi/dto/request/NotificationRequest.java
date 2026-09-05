package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank
    @Size(min = 1, max = 2000)
    private String message;

    @NotNull
    private NotificationType notificationType;

    @NotBlank
    @Size(min = 1, max = 200)
    private String title;
}
