package com.example.bookingmanagementapi.dto.filter;


import com.example.bookingmanagementapi.enums.Tickets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatFilter {
    private Long flightId;
    private String seatNumber;
    private Tickets ticketClass;
    private Boolean isAvailable;
}
