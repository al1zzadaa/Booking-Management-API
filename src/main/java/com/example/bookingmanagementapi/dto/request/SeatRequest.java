package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.Tickets;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatRequest {
    private Long flightId;
    private String seat;
    private Rows seatRow;
    private Integer seatNumber;
    private Tickets ticketClass;
}
