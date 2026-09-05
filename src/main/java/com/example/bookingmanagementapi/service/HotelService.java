package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.HotelFilter;
import com.example.bookingmanagementapi.dto.request.HotelRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelService {

    void createHotel(HotelRequest hotelRequest);

    void deleteHotel(Long hotelId);

    Page<@NonNull HotelResponse> findAll(HotelFilter hotelFilter, Pageable pageable);

    HotelResponse findById(Long id);
}
