package com.example.bookingmanagementapi.dto.response.hotel;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HotelPaymentResponse {
    private String paymentId;
    private String bookingId;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;

}
