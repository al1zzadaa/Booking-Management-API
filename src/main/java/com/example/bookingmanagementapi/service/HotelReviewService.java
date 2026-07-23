package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;

import java.util.List;

public interface HotelReviewService {

    void createHotelReview(HotelReviewRequest hotelReviewRequest);

    void updateHotelReview(Long id, UpdateHotelReviewRequest updateHotelReviewRequest);

    void deleteHotelReview(Long id);

    List<HotelReviewResponse> findAll(HotelReviewFilter hotelReviewFilter);

    HotelReviewResponse findById(Long id);
}
