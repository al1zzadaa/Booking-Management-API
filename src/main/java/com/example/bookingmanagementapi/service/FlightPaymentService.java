package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.FlightPaymentFilter;
import com.example.bookingmanagementapi.dto.request.FlightPaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightPaymentRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightPaymentResponse;

import java.util.List;

public interface FlightPaymentService {

    void payment(FlightPaymentRequest flightPaymentRequest);

    void delete(Long id);

    void update(UpdateFlightPaymentRequest updateFlightPaymentRequest, Long id);

    FlightPaymentResponse findById(Long id);

    List<FlightPaymentResponse> getAll(FlightPaymentFilter flightPaymentFilter);

}
