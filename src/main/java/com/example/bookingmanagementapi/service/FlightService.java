package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.filter.FlightFilter;
import com.example.bookingmanagementapi.dto.request.FlightRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

    void createFlight(FlightRequest flightRequest);

    void deleteFlight(Long flightId);

    Page<@NonNull FlightResponse> findAll(FlightFilter flightFilter, Pageable pageable);

    FlightResponse findById(Long id);
}
