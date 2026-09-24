package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.HotelReviewService;
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
@RequestMapping("/hotel-reviews")
@RequiredArgsConstructor
public class HotelReviewController {

    private final HotelReviewService hotelReviewService;

    @PostMapping
    public void createHotelReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @RequestBody HotelReviewRequest hotelReviewRequest) {
        hotelReviewService.createHotelReview(userDetails.getId(), hotelReviewRequest);
    }

    @GetMapping("/{id}")
    public HotelReviewResponse getHotelReviewById(@PathVariable Long id) {
        return hotelReviewService.findById(id);
    }

    @PutMapping("/{id}")
    public void updateHotelReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @RequestBody UpdateHotelReviewRequest updateHotelReviewRequest,
                                  @PathVariable @Positive Long id) {
        hotelReviewService.updateHotelReview(userDetails.getId(), id, updateHotelReviewRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteHotelReview(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable @Positive Long id) {
        hotelReviewService.deleteHotelReview(userDetails.getId(), id);
    }

    @GetMapping("/search")
    public Page<@NonNull HotelReviewResponse> getAllHotelReviews(HotelReviewFilter hotelReviewFilter, Pageable pageable) {
        return hotelReviewService.findAll(hotelReviewFilter, pageable);
    }

}
