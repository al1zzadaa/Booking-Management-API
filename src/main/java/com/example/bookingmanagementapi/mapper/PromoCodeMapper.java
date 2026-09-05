package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromoCodeMapper {

    PromoCodeResponse toDto(PromoCodeEntity entity);

    PromoCodeEntity toEntity(PromoCodeRequest request);

    List<PromoCodeResponse> toDto(List<PromoCodeEntity> entities);

    void update(PromoCodeRequest request,  @MappingTarget PromoCodeEntity entity);

}
