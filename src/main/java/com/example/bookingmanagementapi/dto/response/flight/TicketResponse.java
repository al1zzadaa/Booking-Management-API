package com.example.bookingmanagementapi.dto.response.flight;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TicketResponse {
    private Long id;
    private Long userId;
    private Long flightId;
    private String seatNo;
    private String ticketClass;
    private BigDecimal price;
    private String status;
    private LocalDateTime createdAt;
    private Integer baggage;
    private Integer fare;
}
