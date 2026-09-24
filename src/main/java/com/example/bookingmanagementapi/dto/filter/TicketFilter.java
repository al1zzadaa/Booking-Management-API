package com.example.bookingmanagementapi.dto.filter;

import com.example.bookingmanagementapi.enums.TicketClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketFilter {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long flightId;
    private TicketClass ticketClass;
}
