package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.AirlineFilter;
import com.example.bookingmanagementapi.dto.request.AirlineRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAirlineRequest;
import com.example.bookingmanagementapi.dto.response.AirlineResponse;
import com.example.bookingmanagementapi.service.AirlineService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/airlines")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    @GetMapping
    public Page<@NonNull AirlineResponse> getAirlines(AirlineFilter airlineFilter, Pageable pageable) {
        return airlineService.getAll(airlineFilter, pageable);
    }

    @GetMapping("/{id}")
    public AirlineResponse getAirlineById(@PathVariable @Positive Long id) {
        return airlineService.getById(id);
    }

    @PostMapping
    public void create(@Valid @RequestBody AirlineRequest airlineRequest) {
        airlineService.create(airlineRequest);
    }

    @PutMapping("/{id}")
    public void update(@Valid @RequestBody UpdateAirlineRequest updateAirlineRequest,
                       @PathVariable @Positive Long id) {
        airlineService.update(updateAirlineRequest, id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @Positive Long id) {
        airlineService.delete(id);
    }

    @PatchMapping("/{id}")
    public void activate(@PathVariable @Positive Long id) {
        airlineService.activate(id);
    }
}
