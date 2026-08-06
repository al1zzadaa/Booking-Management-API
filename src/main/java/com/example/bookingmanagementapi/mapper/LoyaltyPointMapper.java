package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoyaltyPointMapper {

    @Mapping(source = "user.id", target = "userId")
    LoyaltyPointResponse toDto(LoyaltyPointEntity loyaltyPointEntity);

    @Mapping(source = "userId", target = "user.id")
    LoyaltyPointEntity toEntity(LoyaltyPointRequest loyaltyPointRequest);

    @Mapping(source = "user.id", target = "userId")
    List<LoyaltyPointResponse> toListDto(List<LoyaltyPointEntity> loyaltyPointEntityList);
}
