package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FareBaggageRequest;
import com.example.bookingmanagementapi.dto.response.FareBaggageResponse;
import com.example.bookingmanagementapi.entity.FareBaggageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FareBaggageMapper {

    FareBaggageEntity toEntity(FareBaggageRequest fareBaggageRequest);

    FareBaggageResponse toDto(FareBaggageEntity fareBaggageEntity);

    List<FareBaggageResponse> toListDto(List<FareBaggageEntity> fareBaggageEntities);
}
