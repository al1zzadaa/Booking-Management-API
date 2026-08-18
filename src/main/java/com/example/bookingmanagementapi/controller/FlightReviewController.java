package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.FlightReviewFilter;
import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import com.example.bookingmanagementapi.service.FlightReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/flight-reviews")
@RequiredArgsConstructor
public class FlightReviewController {

    private final FlightReviewService flightReviewService;

    @DeleteMapping("/{id}")
    public void deleteFlightReview(@PathVariable Long id) {
        flightReviewService.deleteFlightReview(id);
    }

    @PutMapping("/{id}")
    public void updateFlightReview(@RequestBody UpdateFlightReviewRequest updateFlightReviewRequest,
                                   @PathVariable Long id) {
        flightReviewService.updateFlightReview(id, updateFlightReviewRequest);
    }

    @PostMapping
    public void createFlightReview(@RequestBody FlightReviewRequest flightReviewRequest) {
        flightReviewService.createFlightReview(flightReviewRequest);
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
