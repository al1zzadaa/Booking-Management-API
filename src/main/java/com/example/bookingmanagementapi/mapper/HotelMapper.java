package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.HotelRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse;
import com.example.bookingmanagementapi.entity.HotelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(source = "id", target = "hotelId")
    @Mapping(source = "starRating", target = "stars")
    HotelResponse toDto(HotelEntity hotelEntity);

    @Mapping(source = "stars", target = "starRating")
    HotelEntity toEntity(HotelRequest hotelRequest);


    List<HotelResponse> toListDto(List<HotelEntity> hotelEntities);

    void updateHotel(UpdateHotelRequest updateHotelRequest,  @MappingTarget HotelEntity hotelEntity);

}
