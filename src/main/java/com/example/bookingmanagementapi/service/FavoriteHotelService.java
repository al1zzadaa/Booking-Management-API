package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FavoriteHotelService {

    void addFavoriteHotel(Long userId, FavoriteHotelRequest favoriteHotelRequest);

    void removeFavoriteHotel(Long userId, Long id);

    Page<@NonNull FavoriteHotelResponse> getAll(Long userId, Pageable pageable);

    void clearFavoriteHotels(Long userId);

    FavoriteHotelResponse getById(Long id);
}