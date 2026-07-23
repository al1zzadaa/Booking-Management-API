package com.example.bookingmanagementapi.dto.filter;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightFilter {
    private String departureCountry;
    private String arrivalCountry;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;
    private Long airlineId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
