package com.example.bookingmanagementapi.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReviewFilter {
    private String hotelName;
    private Long hotelId;
    private Long userId;
    private Integer fromRating;
    private Integer toRating;
    private LocalDate fromDate;
    private LocalDate toDate;
}
