package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.filter.FlightReviewFilter;
import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightReviewService {

    void createFlightReview(Long userId, FlightReviewRequest flightReviewRequest);

    void updateFlightReview(Long id, Long userId, UpdateFlightReviewRequest updateFlightReviewRequest);

    void deleteFlightReview(Long userId, Long id);

    Page<@NonNull FlightReviewResponse> findAll(FlightReviewFilter flightReviewFilter, Pageable pageable);

    FlightReviewResponse findById(Long id);
}
