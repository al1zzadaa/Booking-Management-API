package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHotelRequest {
    private String hotelName;
    private String country;
    private String city;
    private String hotelAddress;
    private Integer stars;
    private String description;
    private Integer distanceToSea;
}
