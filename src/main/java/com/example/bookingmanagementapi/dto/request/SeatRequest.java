package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.Tickets;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatRequest {

    @NotNull
    @Positive
    private Long flightId;

    @NotBlank
    @Size(max = 5)
    private String seat;

    @NotNull
    private Rows seatRow;

    @NotNull
    @Positive
    private Integer seatNumber;

    @NotNull
    private Tickets ticketClass;
}
