package com.example.bookingmanagementapi.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelRequest {
    private String hotelName;
    private String country;
    private String city;
    private String hotelAddress;
    private Integer stars;
    private String description;
    private Integer distanceToSea;
}
