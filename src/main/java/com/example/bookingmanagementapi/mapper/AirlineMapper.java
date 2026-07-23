package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.AirlineRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAirlineRequest;
import com.example.bookingmanagementapi.dto.response.AirlineResponse;
import com.example.bookingmanagementapi.entity.AirlineEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AirlineMapper {

    @Mapping(source = "airlineName", target = "name")
    AirlineResponse toDto(AirlineEntity airlineEntity);

    @Mapping(source = "name", target = "airlineName")
    AirlineEntity toEntity(AirlineRequest airlineRequest);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(UpdateAirlineRequest updateAirlineRequest, @MappingTarget AirlineEntity airlineEntity);
}
