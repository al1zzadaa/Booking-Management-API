package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse;
import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = SubscriptionPlanMapper.class)
public interface SubscriptionMapper {

    SubscriptionEntity toEntity(SubscriptionRequest subscriptionRequest);

    @Mapping(source = "user.id", target = "userId")
    SubscriptionResponse toDto(SubscriptionEntity subscriptionEntity);
}
