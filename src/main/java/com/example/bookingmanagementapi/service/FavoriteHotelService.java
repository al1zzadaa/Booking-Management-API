package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;

import java.util.List;

public interface FavoriteHotelService {

    void addFavoriteHotel(FavoriteHotelRequest favoriteHotelRequest);

    void removeFavoriteHotel(Long id);

    List<FavoriteHotelResponse> getAll(Long userId);

}