package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteFlightService {

    void addFavoriteFlight(FavoriteFlightRequest favoriteFlightRequest);

    void removeFavoriteFlight(Long id);

    Page<@NonNull FavoriteFlightResponse> getAll(Long userId, Pageable pageable);

    void clearFavoriteFlights(Long userId);

    FavoriteFlightResponse getById(Long id);

    void updateFavoriteFlight(Long id, UpdateFavoriteFlightRequest updateFavoriteFlightRequest);
}
