package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionEntity toEntity(SubscriptionRequest subscriptionRequest);

    SubscriptionResponse toDto(SubscriptionEntity subscriptionEntity);
}
