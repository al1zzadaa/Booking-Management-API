package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BookingRequest {

    @NotNull
    @Positive
    private Long accountId;

    @NotNull
    @Positive
    private Long hotel;

    @NotNull
    @Positive
    private Long room;

    @NotNull
    @Future
    private LocalDateTime checkIn;

    @NotNull
    @Future
    private LocalDateTime checkOut;

    @NotNull
    @Min(1)
    @Max(15)
    private Integer adultNumber;

    @Size(max = 10)
    private List<@Min(0) @Max(17) Integer> childrenAges;
}