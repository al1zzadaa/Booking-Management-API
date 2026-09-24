package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelReviewService {

    void createHotelReview(Long userId, HotelReviewRequest hotelReviewRequest);

    void updateHotelReview(Long userId, Long id,UpdateHotelReviewRequest updateHotelReviewRequest);

    void deleteHotelReview(Long userId, Long id);

    Page<@NonNull HotelReviewResponse> findAll(HotelReviewFilter hotelReviewFilter, Pageable pageable);

    HotelReviewResponse findById(Long id);
}
