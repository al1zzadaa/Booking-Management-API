package com.example.bookingmanagementapi.dto.response.flight;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FlightPaymentResponse {
    private String paymentId;
    private String ticketId;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}
