package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.response.FlightBookingResponse;
import com.example.bookingmanagementapi.entity.FlightBookingEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlightBookingMapper {

    FlightBookingResponse toDto(FlightBookingEntity flightBookingEntity);
}
