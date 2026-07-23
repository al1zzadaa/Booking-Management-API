package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightReviewMapper {

    FlightReviewEntity toEntity(FlightReviewRequest FlightReviewRequest);

    FlightReviewResponse toDto(FlightReviewEntity flightReview);

    void updateFlightReview(UpdateFlightReviewRequest updateFlightReviewRequest, @MappingTarget FlightReviewEntity flightReviewEntity);

    List<FlightReviewResponse> toListDto(List<FlightReviewEntity> flightReviewsEntity);
}
