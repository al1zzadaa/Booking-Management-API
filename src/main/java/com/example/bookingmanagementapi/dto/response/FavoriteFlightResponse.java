package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class FavoriteFlightResponse {
    private Long id;
    private Long flightId;
    private Long userId;
    private FlightResponse flight;
}
