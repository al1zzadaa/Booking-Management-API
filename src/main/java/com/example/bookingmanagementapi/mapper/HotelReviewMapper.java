package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;
import com.example.bookingmanagementapi.entity.HotelReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HotelReviewMapper {

    @Mapping(source = "id", target = "reviewId")
    HotelReviewResponse toDto(HotelReviewEntity hotelReviewEntity);

    HotelReviewEntity toEntity(HotelReviewRequest hotelReviewRequest);

    void updateHotelReview(UpdateHotelReviewRequest updateHotelReviewRequest, @MappingTarget HotelReviewEntity hotelReviewEntity);
}
