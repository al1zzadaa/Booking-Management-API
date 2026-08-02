package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateBookingRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController{

    private final BookingService bookingService;

    @PostMapping("/pay/{bookingId}")
    public void payBooking(@PathVariable Long bookingId, @RequestBody PaymentRequest request) {
        bookingService.payBooking(bookingId, request);
    }

    @PostMapping("/cancel/{bookingId}")
    public void cancel(@PathVariable Long bookingId){
        bookingService.cancel(bookingId);
    }

    @PostMapping
    public void bookHotel(@RequestBody BookingRequest bookingRequest){
        bookingService.bookHotel(bookingRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id){
        bookingService.deleteBooking(id);
    }

    @PutMapping("/{id}")
    public void updateBooking(@RequestBody UpdateBookingRequest updateBookingRequest, @PathVariable Long id){
        bookingService.updateBooking(updateBookingRequest, id);
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable Long id){
        return bookingService.getBookingById(id);
    }

    @GetMapping
    public Page<BookingResponse> getAllBooking(BookingFilter bookingFilter,  Pageable pageable){
        return bookingService.getBookings(bookingFilter, pageable);
    }
}
