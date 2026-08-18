package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.service.FavoriteFlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite-flights")
@RequiredArgsConstructor
public class FavoriteFlightController {

    private final FavoriteFlightService favoriteFlightService;

    @PostMapping
    public void addFlight(@RequestBody FavoriteFlightRequest favoriteFlightRequest) {
        favoriteFlightService.addFavoriteFlight(favoriteFlightRequest);
    }

    @GetMapping("/{userId}")
    public Page<FavoriteFlightResponse> getFavoriteFlights(@PathVariable Long userId, Pageable pageable) {
        return favoriteFlightService.getAll(userId, pageable);
    }

    @DeleteMapping("/{id}")
    public void removeFlight(@PathVariable Long id) {
        favoriteFlightService.removeFavoriteFlight(id);
    }


}
