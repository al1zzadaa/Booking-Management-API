package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Flights;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FlightRequest {
    private String departureCountry;
    private String departureCity;
    private String arrivalCountry;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;

    private Long airlineId;
    private Long flightNumber;
    private Flights status;

    private BigDecimal basePrice;
}
