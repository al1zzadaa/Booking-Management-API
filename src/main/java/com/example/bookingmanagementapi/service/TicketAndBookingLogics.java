package com.example.bookingmanagementapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TicketAndBookingLogics {

    BigDecimal getBigDecimal(long daysLeft,
                             BigDecimal refund);

    BigDecimal calculateRefund(
            BigDecimal amount,
            LocalDateTime eventDate,
            String alreadyStartedMessage);

    BigDecimal getTotalPrice(
            Integer days,
            Integer adultNumber,
            Integer childNumber,
            BigDecimal pricePerNight);
}
