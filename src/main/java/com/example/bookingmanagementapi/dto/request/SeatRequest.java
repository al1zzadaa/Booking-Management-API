package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.TicketClass;
import jakarta.validation.constraints.*;
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
    @Min(0)
    @Max(20)
    private Integer seatNumber;

    @NotNull
    private TicketClass ticketClass;
}
