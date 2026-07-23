package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoyaltyPointMapper {

    LoyaltyPointResponse toDto(LoyaltyPointEntity loyaltyPointEntity);

    LoyaltyPointEntity toEntity(LoyaltyPointRequest loyaltyPointRequest);

    List<LoyaltyPointResponse> toListDto(List<LoyaltyPointEntity> loyaltyPointEntityList);
}
