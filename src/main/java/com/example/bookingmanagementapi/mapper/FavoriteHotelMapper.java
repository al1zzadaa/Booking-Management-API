package com.example.bookingmanagementapi.mapper;


import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import com.example.bookingmanagementapi.entity.FavoriteHotelEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FavoriteHotelMapper {

    FavoriteHotelResponse toResponse(FavoriteHotelEntity entity);
}