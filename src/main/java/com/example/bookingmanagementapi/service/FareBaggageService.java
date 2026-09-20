package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.request.FareBaggageRequest;
import com.example.bookingmanagementapi.dto.response.FareBaggageResponse;

import java.util.List;

public interface FareBaggageService {

    void createFareBaggage(FareBaggageRequest fareBaggageRequest);

    void deleteFareBaggageById(Long id);

    FareBaggageResponse findById(Long id);

    List<FareBaggageResponse> getAll();

}
