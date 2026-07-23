package com.example.bookingmanagementapi.dto.request;


import com.example.bookingmanagementapi.enums.Tickets;

import java.math.BigDecimal;

public class FareBaggageRequest {
    private Long airlineId;
    private Tickets ticket;
    private Integer baggage;
    private Integer fare;
    private BigDecimal price;
}
