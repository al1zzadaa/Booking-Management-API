package com.example.bookingmanagementapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirlineResponse {
    private String name;
    private String model;
    private String country;
}
