package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.SeatFilter;
import com.example.bookingmanagementapi.dto.request.SeatRequest;
import com.example.bookingmanagementapi.dto.request.UpdateSeatRequest;
import com.example.bookingmanagementapi.dto.response.flight.SeatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SeatService {

    void createSeat(SeatRequest seatRequest);

    void updateSeat(Long id, UpdateSeatRequest updateSeatRequest);

    void deleteSeat(Long id);

    SeatResponse getSeat(Long id);

    Page<SeatResponse> getSeats(SeatFilter seatFilter, Pageable pageable);
}
