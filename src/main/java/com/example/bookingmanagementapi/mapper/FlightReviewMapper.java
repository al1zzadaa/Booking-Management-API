package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "flightId", target = "flight.id")
    FlightReviewEntity toEntity(FlightReviewRequest FlightReviewRequest);

    FlightReviewResponse toDto(FlightReviewEntity flightReview);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFlightReview(UpdateFlightReviewRequest updateFlightReviewRequest, @MappingTarget FlightReviewEntity flightReviewEntity);
}
