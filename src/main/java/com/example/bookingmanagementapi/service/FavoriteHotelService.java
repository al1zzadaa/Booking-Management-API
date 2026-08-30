package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;

import java.util.List;

public interface FavoriteHotelService {

    void addFavoriteHotel(Long userId, FavoriteHotelRequest favoriteHotelRequest);

    void removeFavoriteHotel(Long userId, Long id);

    List<FavoriteHotelResponse> getAll(Long userId);

    void clearFavoriteHotels(Long userId);

    FavoriteHotelResponse getById(Long id);
}