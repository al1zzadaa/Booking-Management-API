package com.example.bookingmanagementapi.dto.response.hotel;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HotelResponse {
    private Long hotelId;
    private String hotelName;
    private String country;
    private String city;
    private String hotelAddress;
    private Integer stars;
    private String description;
    private Integer distanceToSea;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HotelReviewResponse> reviews;
}
