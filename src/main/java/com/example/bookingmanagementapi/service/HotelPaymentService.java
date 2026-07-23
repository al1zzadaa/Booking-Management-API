package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.filter.HotelPaymentFilter;
import com.example.bookingmanagementapi.dto.request.HotelPaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelPaymentRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelPaymentResponse;

import java.util.List;

public interface HotelPaymentService {

    void payment(HotelPaymentRequest hotelPaymentRequest);

    void delete(Long id);

    void update(UpdateHotelPaymentRequest updateHotelPaymentRequest, Long id);

    HotelPaymentResponse findById(Long id);

    List<HotelPaymentResponse> getAll(HotelPaymentFilter hotelPaymentFilter);

}
