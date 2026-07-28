package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {
    private String message;
    private NotificationType notificationType;
    private Boolean isRead;
    private String title;
}
