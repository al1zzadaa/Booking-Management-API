package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.entity.FavoriteFlightEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = FlightMapper.class)
public interface FavoriteFlightMapper {

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "flightId", target = "flight.id")
    FavoriteFlightEntity toEntity(FavoriteFlightRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "flightId", source = "flight.id")
//    @Mapping(target = "flight", source = "flight")
    FavoriteFlightResponse toResponse(FavoriteFlightEntity entity);

    List<FavoriteFlightResponse> toListDto(List<FavoriteFlightEntity> list);

    void updateFavoriteFlight(UpdateFavoriteFlightRequest updateFavoriteFlightRequest, @MappingTarget FavoriteFlightEntity favoriteFlightEntity);
}
