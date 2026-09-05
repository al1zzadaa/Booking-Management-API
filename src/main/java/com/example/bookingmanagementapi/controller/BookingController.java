package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.service.BookingService;
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
                           @PathVariable Long bookingId,
                           @RequestBody PaymentRequest request) {
        bookingService.payBooking(userDetails.getUsername(), bookingId, request);
    }

    @PostMapping("/cancel/{bookingId}")
    public void cancel(@AuthenticationPrincipal UserDetails userDetails,
                       @PathVariable Long bookingId) {
        bookingService.cancel(userDetails.getUsername(),bookingId);
    }

    @PostMapping
    public void bookHotel(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestBody BookingRequest bookingRequest) {
        bookingService.bookHotel(userDetails.getUsername(), bookingRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }


    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    @GetMapping
    public Page<BookingResponse> getAllBooking(BookingFilter bookingFilter, Pageable pageable) {
        return bookingService.getBookings(bookingFilter, pageable);
    }

    @GetMapping("/my-history")
    public Page<BookingResponse> getMyBookings(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return bookingService.getUserBookings(userDetails.getUsername(), pageable);
    }
}
