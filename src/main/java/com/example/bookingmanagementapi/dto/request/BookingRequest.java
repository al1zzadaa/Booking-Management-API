package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequest {
    private Long userId;
    private Long hotel;
    private Long room;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer peopleNumber;
}
