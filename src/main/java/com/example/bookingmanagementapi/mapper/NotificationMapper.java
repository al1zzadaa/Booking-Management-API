package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.NotificationRequest;
import com.example.bookingmanagementapi.dto.request.UpdateNotificationRequest;
import com.example.bookingmanagementapi.dto.response.NotificationResponse;
import com.example.bookingmanagementapi.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toDto(NotificationEntity notificationEntity);

    List<NotificationResponse> toListDto(List<NotificationEntity> notificationEntityList);

    NotificationEntity toEntity(NotificationRequest notificationRequest);

    void updateNotification(UpdateNotificationRequest updateNotificationRequest, @MappingTarget NotificationEntity notificationEntity);
}
