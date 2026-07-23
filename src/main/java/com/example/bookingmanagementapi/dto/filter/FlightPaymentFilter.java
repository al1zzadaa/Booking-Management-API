package com.example.bookingmanagementapi.dto.filter;

import com.example.bookingmanagementapi.enums.PaymentMethods;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightPaymentFilter {
    private Long flightId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private LocalDate fromDate;
    private LocalDate toDate;
    private PaymentStatus paymentStatus;
    private PaymentMethods paymentMethod;
}
