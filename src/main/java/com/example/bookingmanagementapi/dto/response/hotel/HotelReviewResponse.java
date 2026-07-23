package com.example.bookingmanagementapi.dto.response.hotel;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HotelReviewResponse {
    private String reviewId;
    private Long userId;
    private String hotelId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
