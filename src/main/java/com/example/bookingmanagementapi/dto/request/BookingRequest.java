package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BookingRequest {
    private Long accountId;
    private Long hotel;
    private Long room;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer adultNumber;
//    private Integer childrenNumber;
    private List<@Min(0) @Max(17)Integer> childrenAges;
}