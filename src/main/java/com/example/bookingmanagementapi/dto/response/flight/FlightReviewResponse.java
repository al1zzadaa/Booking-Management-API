package com.example.bookingmanagementapi.dto.response.flight;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FlightReviewResponse {
    private String reviewId;
    private String userId;
    private String flightId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
