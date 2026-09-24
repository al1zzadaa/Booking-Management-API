package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.FlightFilter;
import com.example.bookingmanagementapi.dto.request.FlightRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import com.example.bookingmanagementapi.service.FlightService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public void createFlight(@Valid @RequestBody FlightRequest flightRequest) {
        flightService.createFlight(flightRequest);
    }

    @GetMapping("/{id}")
    public FlightResponse getFlightById(@PathVariable @Positive Long id){
        return flightService.findById(id);
    }

    @GetMapping
    public Page<@NonNull FlightResponse> getAllFlights(FlightFilter flightFilter, Pageable pageable){
        return flightService.findAll(flightFilter, pageable);
    }

    @DeleteMapping("/{id}")
    public void deleteFlightById(@PathVariable @Positive Long id){
        flightService.deleteFlight(id);
    }
}
