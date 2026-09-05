package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.SubscriptionPlanRequest;
import com.example.bookingmanagementapi.dto.response.SubscriptionPlanResponse;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {

    SubscriptionPlanEntity toEntity(SubscriptionPlanRequest subscriptionPlanRequest);

    SubscriptionPlanResponse toDto(SubscriptionPlanEntity subscriptionPlanEntity);

    List<SubscriptionPlanResponse> toListDto(List<SubscriptionPlanEntity> subscriptionPlanEntityList);

    void update(SubscriptionPlanRequest updateSubscriptionPlanRequest, @MappingTarget SubscriptionPlanEntity subscriptionPlanEntity);
}
