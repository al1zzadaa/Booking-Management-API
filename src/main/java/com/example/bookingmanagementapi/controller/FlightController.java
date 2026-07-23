package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.filter.FlightFilter;
import com.example.bookingmanagementapi.dto.request.FlightRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import com.example.bookingmanagementapi.service.FlightService;
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
    public void createFlight(@RequestBody FlightRequest flightRequest) {
        flightService.createFlight(flightRequest);
    }

    @GetMapping("/{id}")
    public FlightResponse getFlightById(@PathVariable Long id){
        return flightService.findById(id);
    }

    @GetMapping
    public Page<FlightResponse> getAllFlights(FlightFilter flightFilter, Pageable pageable){
        return flightService.findAll(flightFilter, pageable);
    }

    @PutMapping("/{id}")
    public void updateFlight(@RequestBody UpdateFlightRequest updateFlightRequest, @PathVariable Long id){
        flightService.updateFlight(id, updateFlightRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteFlightById(@PathVariable Long id){
        flightService.deleteFlight(id);
    }
}
