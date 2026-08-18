package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.HotelFilter;
import com.example.bookingmanagementapi.dto.request.HotelRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse;
import com.example.bookingmanagementapi.service.HotelService;
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
    public void createHotel(@RequestBody HotelRequest hotelRequest) {
        hotelService.createHotel(hotelRequest);
    }

    @GetMapping("/{id}")
    public HotelResponse getHotelById(@PathVariable Long id) {
        return hotelService.findById(id);
    }

    @PutMapping("/{id}")
    public void updateHotel(@RequestBody UpdateHotelRequest updateHotelRequest,
                            @PathVariable Long id) {
        hotelService.updateHotel(id, updateHotelRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteHotelById(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }

    @GetMapping
    public Page<HotelResponse> getAllHotels(HotelFilter hotelFilter, Pageable pageable) {
        return hotelService.findAll(hotelFilter, pageable);
    }
}
