package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirlineRequest {
    private String name;
    private String model;
    private String country;
}
