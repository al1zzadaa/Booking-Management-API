package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateBookingRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    void bookHotel(BookingRequest bookingRequest);

    void updateBooking(UpdateBookingRequest updateBookingRequest, Long id);

    void deleteBooking(Long id);

    BookingResponse getBookingById(Long id);

    Page<@NonNull BookingResponse> getBookings(BookingFilter bookingFilter, Pageable pageable);

    void cancelBooking(Long bookingId);

    void payBooking(Long bookingId, PaymentRequest request);

    void cancel(Long bookingId);
}
