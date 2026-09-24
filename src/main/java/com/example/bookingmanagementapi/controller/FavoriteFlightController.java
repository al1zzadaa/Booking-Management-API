package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.FavoriteFlightService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite-flights")
@RequiredArgsConstructor
public class FavoriteFlightController {

    private final FavoriteFlightService favoriteFlightService;

    @PostMapping
    public void addFlight(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @Valid @RequestBody FavoriteFlightRequest favoriteFlightRequest) {
        favoriteFlightService.addFavoriteFlight(userDetails.getId(), favoriteFlightRequest);
    }

    @GetMapping("/{userId}")
    public Page<@NonNull FavoriteFlightResponse> getFavoriteFlights(@PathVariable @Positive Long userId,
                                                                    Pageable pageable) {
        return favoriteFlightService.getAll(userId, pageable);
    }

    @DeleteMapping("/{id}")
    public void removeFlight(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable @Positive Long id) {
        favoriteFlightService.removeFavoriteFlight(userDetails.getId(), id);
    }


}
