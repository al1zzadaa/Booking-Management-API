package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.entity.AirlineEntity;
import com.example.bookingmanagementapi.enums.TicketClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FareBaggageResponse {
    private Long id;
    private AirlineEntity airline;
    private Integer baggage;
    private Integer handLuggage;
    private BigDecimal price;
    private TicketClass ticketClass;
}
