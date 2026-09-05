package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.FareBaggageFilter;
import com.example.bookingmanagementapi.dto.request.FareBaggageRequest;
import com.example.bookingmanagementapi.dto.response.FareBaggageResponse;
import com.example.bookingmanagementapi.service.FareBaggageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fare-baggage")
@RequiredArgsConstructor
public class FareBaggageController {

    private final FareBaggageService fareBaggageService;

    @PostMapping
    public void createFareBaggage(@RequestBody FareBaggageRequest fareBaggageRequest) {
        fareBaggageService.createFareBaggage(fareBaggageRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteFareBaggage(@PathVariable Long id) {
        fareBaggageService.deleteFareBaggageById(id);
    }

    @GetMapping("/{id}")
    public FareBaggageResponse getById(@PathVariable Long id) {
        return fareBaggageService.findById(id);
    }

    @GetMapping
    public List<FareBaggageResponse> getAll(FareBaggageFilter fareBaggageFilter) {
        return fareBaggageService.getAll(fareBaggageFilter);
    }

}
