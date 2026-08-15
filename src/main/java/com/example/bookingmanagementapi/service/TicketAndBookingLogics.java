package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.entity.RoomEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TicketAndBookingLogics {

    BigDecimal getBigDecimal(long daysLeft,
                             BigDecimal refund);

    BigDecimal calculateRefund(
            BigDecimal amount,
            LocalDateTime eventDate,
            String alreadyStartedMessage);

    BigDecimal getTotalPrice(
            Long days,
            Integer adultNumber,
            List<Integer> childrenAges,
            RoomEntity roomEntity);
}
