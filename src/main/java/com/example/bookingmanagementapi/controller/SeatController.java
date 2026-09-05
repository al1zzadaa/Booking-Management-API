package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.SeatFilter;
import com.example.bookingmanagementapi.dto.request.SeatRequest;
import com.example.bookingmanagementapi.dto.response.flight.SeatResponse;
import com.example.bookingmanagementapi.service.SeatService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @DeleteMapping("/{id}")
    public void deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
    }

    @PostMapping
    public void createSeat(@RequestBody SeatRequest seatRequest) {
        seatService.createSeat(seatRequest);
    }

    @GetMapping("/{id}")
    public SeatResponse getSeatById(@PathVariable Long id) {
        return seatService.getSeat(id);
    }

    @GetMapping
    public Page<@NonNull SeatResponse> getSeats(SeatFilter seatFilter, Pageable pageable) {
        return seatService.getSeats(seatFilter, pageable);
    }

}
