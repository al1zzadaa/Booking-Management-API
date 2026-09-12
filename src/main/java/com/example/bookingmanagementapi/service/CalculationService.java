package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.entity.RoomEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CalculationService {

    BigDecimal calculateBookingRefund(Long bookingId);

    BigDecimal calculateTicketRefund(Long flightBookingId);

    BigDecimal calculateFlightTotalPrice(
            TicketRequest request,
            FlightEntity flight);

    BigDecimal getBigDecimal(long daysLeft,
                             BigDecimal refund);

    BigDecimal calculateRefund(
            BigDecimal amount,
            LocalDateTime eventDate,
            String alreadyStartedMessage);

    BigDecimal getBookingTotalPrice(
            Long days,
            Integer adultNumber,
            List<Integer> childrenAges,
            RoomEntity roomEntity);
}
