package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import com.example.bookingmanagementapi.service.FavoriteHotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite-hotels")
@RequiredArgsConstructor
public class FavoriteHotelController {

    private final FavoriteHotelService favoriteHotelService;

    @PostMapping
    public void addHotel(@RequestBody FavoriteHotelRequest favoriteHotelRequest) {
        favoriteHotelService.addFavoriteHotel(favoriteHotelRequest);
    }

    @GetMapping("/{userId}")
    public List<FavoriteHotelResponse> getFavoriteHotels(@PathVariable Long userId) {
        return favoriteHotelService.getAll(userId);
    }

    @DeleteMapping("/{id}")
    public void removeHotel(@PathVariable Long id) {
        favoriteHotelService.removeFavoriteHotel(id);
    }
}
