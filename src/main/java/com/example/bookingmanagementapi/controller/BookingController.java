package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/pay/{bookingId}")
    public void payBooking(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable @Positive Long bookingId,
                           @Valid @RequestBody PaymentRequest request) {
        bookingService.payBooking(userDetails.getUsername(), bookingId, request);
    }

    @PostMapping("/cancel/{bookingId}")
    public void cancel(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable @Positive Long bookingId) {
        bookingService.refundBooking(userDetails.getUsername(),bookingId);
    }

    @PostMapping
    public void bookHotel(@AuthenticationPrincipal UserDetails userDetails,
                          @Valid @RequestBody BookingRequest bookingRequest) {
        bookingService.bookHotel(userDetails.getUsername(), bookingRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable @Positive Long id) {
        bookingService.deleteBooking(id);
    }


    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable @Positive Long id) {
        return bookingService.getBookingById(id);
    }

    @GetMapping
    public Page<@NonNull BookingResponse> getAllBooking(BookingFilter bookingFilter, Pageable pageable) {
        return bookingService.getBookings(bookingFilter, pageable);
    }

    @GetMapping("/my-history")
    public Page<@NonNull BookingResponse> getMyBookings(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return bookingService.getUserBookings(userDetails.getUsername(), pageable);
    }
}
