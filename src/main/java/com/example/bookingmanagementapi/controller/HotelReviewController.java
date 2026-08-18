package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;
import com.example.bookingmanagementapi.service.HotelReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotel-reviews")
@RequiredArgsConstructor
public class HotelReviewController {

    private final HotelReviewService hotelReviewService;

    @PostMapping
    public void createHotelReview(@RequestBody HotelReviewRequest hotelReviewRequest) {
        hotelReviewService.createHotelReview(hotelReviewRequest);
    }

    @GetMapping("/{id}")
    public HotelReviewResponse getHotelReviewById(@PathVariable Long id) {
        return hotelReviewService.findById(id);
    }

    @PutMapping("/{id}")
    public void updateHotelReview(@RequestBody UpdateHotelReviewRequest updateHotelReviewRequest,
                                  @PathVariable Long id) {
        hotelReviewService.updateHotelReview(id, updateHotelReviewRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteHotelReview(@PathVariable Long id) {
        hotelReviewService.deleteHotelReview(id);
    }

    @GetMapping("/search")
    public List<HotelReviewResponse> getAllHotelReviews(HotelReviewFilter hotelReviewFilter) {
        return hotelReviewService.findAll(hotelReviewFilter);
    }

}
