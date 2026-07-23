package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlightReviewRequest {
    private Long userId;
    private Long flightId;
    private String comment;
    private Integer rating;
}
