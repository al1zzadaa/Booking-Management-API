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
import java.util.List;

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
    @Value("${booking.children.infant-max-age}")
    private Integer infantMaxAge;
    @Value("${booking.children.infant-discount-percent}")
    private BigDecimal infantDiscountPercent;
    @Value("${booking.children.young-max-age}")
    private Integer youngChildMaxAge;
    @Value("${booking.children.young-discount-percent}")
    private BigDecimal youngChildDiscountPercent;
    @Value("${booking.children.teen-max-age}")
    private Integer teenChildMaxAge;
    @Value("${booking.children.teen-discount-percent}")
    private BigDecimal teenChildDiscountPercent;


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

//    @Override
//    public BigDecimal getTotalPrice(Integer days,
//                                    Integer adultNumber,
//                                    List<Integer> childrenAges,
//                                    RoomEntity room) {
//
//        BigDecimal adultTotal = room.getAdultPrice()
//                .multiply(BigDecimal.valueOf(adultNumber));
//
//        BigDecimal childPrice = room.getAdultPrice()
//                .multiply(
//                        BigDecimal.valueOf(100)
//                                .subtract(room.getChildDiscountPercent())
//                )
//                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//
//        BigDecimal childTotal = childPrice
//                .multiply(BigDecimal.valueOf(childNumber));
//
//        BigDecimal roomTotal = room.getPricePerNight()
//                .multiply(BigDecimal.valueOf(days));
//
//        return roomTotal
//                .add(adultTotal)
//                .add(childTotal);
//    }

    @Override
    public BigDecimal getTotalPrice(Long days,
                                    Integer adultNumber,
                                    List<Integer> childrenAges,
                                    RoomEntity room) {

        BigDecimal adultPrice = room.getAdultPrice();

        BigDecimal adultTotal = adultPrice
                .multiply(BigDecimal.valueOf(adultNumber));

        BigDecimal childTotal = BigDecimal.ZERO;

        for (Integer age : childrenAges) {

            BigDecimal childPrice = calculateChildPrice(room, age, adultPrice);

            childTotal = childTotal.add(childPrice);
        }

        BigDecimal roomTotal = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(days));

        return roomTotal
                .add(adultTotal)
                .add(childTotal);
    }

    private BigDecimal calculateChildPrice(
            RoomEntity room,
            Integer age,
            BigDecimal adultPrice
    ) {
        BigDecimal discountPercent;

        if (age <= infantMaxAge) {
            discountPercent = infantDiscountPercent;
        } else if (age <= youngChildMaxAge) {
            discountPercent = youngChildDiscountPercent;
        } else if (age <= teenChildMaxAge) {
            discountPercent = teenChildDiscountPercent;
        } else {
            discountPercent = room.getChildDiscountPercent();
        }

        return adultPrice
                .multiply(BigDecimal.valueOf(100).subtract(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
