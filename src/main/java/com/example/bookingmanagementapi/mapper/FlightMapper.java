package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FlightRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import com.example.bookingmanagementapi.entity.FlightEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    FlightResponse toDto(FlightEntity flightEntity);

    List<FlightResponse> toListDto(List<FlightEntity> flightEntities);

    FlightEntity toEntity(FlightRequest flightRequest);

    void updateFlight(UpdateFlightRequest updateFlightRequest, @MappingTarget FlightEntity flightEntity);
}
