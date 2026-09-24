package com.example.bookingmanagementapi.dto.filter;


import com.example.bookingmanagementapi.enums.TicketClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatFilter {
    private Long flightId;
    private String seatNumber;
    private TicketClass ticketClass;
    private Boolean isAvailable;
}
