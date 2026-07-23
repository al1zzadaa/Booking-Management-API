package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAirlineRequest {
    private String name;
    private String model;
    private String country;
}
