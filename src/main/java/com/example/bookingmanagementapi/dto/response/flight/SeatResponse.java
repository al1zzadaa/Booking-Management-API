package com.example.bookingmanagementapi.dto.response.flight;

import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.TicketClass;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {
    private Long id;
    private FlightEntity flight;
    private String seat;
    private Rows seatRow;
    private Integer seatNumber;
    private TicketClass ticketClass;
    private BigDecimal price;
    private Boolean isAvailable;
}
