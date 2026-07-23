package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.enums.PaymentMethods;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlightPaymentRequest {
    private Long userId;
    private TicketEntity ticketId;
//    private BigDecimal amount;
    private PaymentMethods paymentMethod;
}
