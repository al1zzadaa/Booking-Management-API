package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "hotel", target = "hotel.id")
    @Mapping(source = "room", target = "room.id")
    BookingEntity toEntity(BookingRequest bookingRequest);

    BookingResponse toDto(BookingEntity bookingEntity);

}
