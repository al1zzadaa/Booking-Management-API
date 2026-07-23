package com.example.bookingmanagementapi.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightReviewFilter {
    private Long airlineId;
    private Long userId;
    private Long flightId;
    private Integer fromRating;
    private Integer toRating;
    private LocalDate fromDate;
    private LocalDate toDate;
}
