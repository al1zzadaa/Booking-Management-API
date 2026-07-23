package com.example.bookingmanagementapi.dto.response.flight;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FlightResponse {
    private String flightId;
    private String departureCountry;
    private String departureCity;
    private String arrivalCountry;
    private String arrivalCity;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;
    private String airlineName;
    private String aircraftModel;
    private Long flightNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> reviews;
}
