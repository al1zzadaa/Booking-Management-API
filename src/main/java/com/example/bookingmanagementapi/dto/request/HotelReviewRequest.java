package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReviewRequest {
    private Long userId;
    private Long hotelId;
    private String comment;
    private Integer rating;
}
