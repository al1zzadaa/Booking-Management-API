package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private UserEntity user;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean read;
}
