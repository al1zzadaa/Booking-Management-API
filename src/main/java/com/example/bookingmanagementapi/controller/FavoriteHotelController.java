package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.FavoriteHotelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite-hotels")
@RequiredArgsConstructor
public class FavoriteHotelController {

    private final FavoriteHotelService favoriteHotelService;

    @PostMapping
    public void addHotel(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                         @Valid @RequestBody FavoriteHotelRequest favoriteHotelRequest) {
        favoriteHotelService.addFavoriteHotel(customUserDetails.getId(), favoriteHotelRequest);
    }

    @GetMapping("/{userId}")
    public Page<@NonNull FavoriteHotelResponse> getFavoriteHotels(@PathVariable @Positive Long userId,
                                                                  Pageable pageable) {
        return favoriteHotelService.getAll(userId,  pageable);
    }

    @DeleteMapping("/{id}")
    public void removeHotel(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                            @PathVariable @Positive Long id) {
        favoriteHotelService.removeFavoriteHotel(customUserDetails.getId(), id);
    }
}
