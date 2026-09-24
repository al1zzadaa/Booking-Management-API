package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FlightRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import com.example.bookingmanagementapi.entity.FlightEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    @Mapping(source = "departureTime", target = "departureDateTime")
    @Mapping(source = "arrivalTime", target = "arrivalDateTime")
    @Mapping(source = "airline.airlineName", target = "airlineName")
    FlightResponse toDto(FlightEntity flightEntity);

    FlightEntity toEntity(FlightRequest flightRequest);
}
