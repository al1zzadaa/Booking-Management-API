package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.entity.FavoriteFlightEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FavoriteFlightMapper {

    FavoriteFlightEntity toEntity(FavoriteFlightRequest request);

    FavoriteFlightResponse toResponse(FavoriteFlightEntity entity);

    List<FavoriteFlightResponse> toListDto(List<FavoriteFlightEntity> list);

    void updateFavoriteFlight(UpdateFavoriteFlightRequest updateFavoriteFlightRequest, @MappingTarget FavoriteFlightEntity favoriteFlightEntity);
}
