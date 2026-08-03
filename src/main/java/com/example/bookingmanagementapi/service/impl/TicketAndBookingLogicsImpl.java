package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.entity.RoomEntity;
import com.example.bookingmanagementapi.service.TicketAndBookingLogics;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class TicketAndBookingLogicsImpl implements TicketAndBookingLogics {
    @Value("${percent10}")
    private Integer percent10;
    @Value("${percent20}")
    private Integer percent20;
    @Value("${percent30}")
    private Integer percent30;
    @Value("${percent50}")
    private Integer percent50;
    @Value("${percent70}")
    private Integer percent70;
    @Value("${adultPrice}")
    private Integer adultPrice;
    @Value("${childPrice}")
    private Integer childPrice;

    @Override
    public BigDecimal getBigDecimal(long daysLeft, BigDecimal refund) {
        int cancellationFee;

        if (daysLeft >= 30) {
            cancellationFee = percent10;
        } else if (daysLeft >= 15) {
            cancellationFee = percent20;
        } else if (daysLeft >= 7) {
            cancellationFee = percent30;
        } else if (daysLeft >= 3) {
            cancellationFee = percent50;
        } else {
            cancellationFee = percent70;
        }

        return refund.multiply(BigDecimal.valueOf(cancellationFee)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }

    @Override
    public BigDecimal calculateRefund(
            BigDecimal amount,
            LocalDateTime eventDate,
            String alreadyStartedMessage
    ) {
        LocalDateTime now = LocalDateTime.now();

        if (eventDate.isBefore(now)) {
            throw new IllegalStateException(alreadyStartedMessage);
        }

        long daysLeft = ChronoUnit.DAYS.between(now, eventDate);
        BigDecimal penalty = getBigDecimal(daysLeft, amount);

        return amount.subtract(penalty);
    }

    @Override
    public BigDecimal getTotalPrice(Integer days,
                                    Integer adultNumber,
                                    Integer childNumber,
                                    RoomEntity room) {

        BigDecimal adultTotal = room.getAdultPrice()
                .multiply(BigDecimal.valueOf(adultNumber));

        BigDecimal childPrice = room.getAdultPrice()
                .multiply(
                        BigDecimal.valueOf(100)
                                .subtract(room.getChildDiscountPercent())
                )
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal childTotal = childPrice
                .multiply(BigDecimal.valueOf(childNumber));

        BigDecimal roomTotal = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(days));

        return roomTotal
                .add(adultTotal)
                .add(childTotal);
    }
}
