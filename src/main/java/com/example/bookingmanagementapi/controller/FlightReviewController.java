package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.FlightReviewFilter;
import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.FlightReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/flight-reviews")
@RequiredArgsConstructor
public class FlightReviewController {

    private final FlightReviewService flightReviewService;

    @PutMapping("/{id}")
    public void updateFlightReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody UpdateFlightReviewRequest request) {

        flightReviewService.updateFlightReview(
                userDetails.getId(),
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public void deleteFlightReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        flightReviewService.deleteFlightReview(
                userDetails.getId(),
                id
        );
    }

    @PostMapping
    public void createFlightReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FlightReviewRequest request) {

        flightReviewService.createFlightReview(
                userDetails.getId(),
                request
        );
    }

    @GetMapping("/{id}")
    public FlightReviewResponse getFlightReviewById(@PathVariable Long id) {
        return flightReviewService.findById(id);
    }

    @GetMapping()
    public Page<FlightReviewResponse> getAllFlightReviews(FlightReviewFilter flightReviewFilter, Pageable pageable) {
        return flightReviewService.findAll(flightReviewFilter, pageable);
    }
}
