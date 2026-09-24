package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.HotelFilter;
import com.example.bookingmanagementapi.dto.request.HotelRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse;
import com.example.bookingmanagementapi.service.HotelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public void createHotel(@Valid @RequestBody HotelRequest hotelRequest) {
        hotelService.createHotel(hotelRequest);
    }

    @GetMapping("/{id}")
    public HotelResponse getHotelById(@PathVariable @Positive Long id) {
        return hotelService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteHotelById(@PathVariable @Positive Long id) {
        hotelService.deleteHotel(id);
    }

    @GetMapping
    public Page<@NonNull HotelResponse> getAllHotels(HotelFilter hotelFilter, Pageable pageable) {
        return hotelService.findAll(hotelFilter, pageable);
    }
}
