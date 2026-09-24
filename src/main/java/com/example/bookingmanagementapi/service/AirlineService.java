package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.AirlineFilter;
import com.example.bookingmanagementapi.dto.request.AirlineRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAirlineRequest;
import com.example.bookingmanagementapi.dto.response.AirlineResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AirlineService {

    void create(AirlineRequest airlineRequest);

    void update(UpdateAirlineRequest updateAirlineRequest, Long id);

    void delete(Long id);

    AirlineResponse getById(Long id);

    Page<@NonNull AirlineResponse> getAll(AirlineFilter airlineFilter, Pageable pageable);

    void activate(Long id);
}
