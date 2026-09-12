package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.PassengerRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.repository.BookingRepository;
import com.example.bookingmanagementapi.repository.FareBaggageRepository;
import com.example.bookingmanagementapi.repository.FlightBookingRepository;
import com.example.bookingmanagementapi.repository.SeatRepository;
import com.example.bookingmanagementapi.service.CalculationService;
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
public class CalculationServiceImpl implements CalculationService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final FareBaggageRepository fareBaggageRepository;
    private final FlightBookingRepository flightBookingRepository;
    @Value("${booking.children.young-max-age}")
    private Integer youngChildMaxAge;
    @Value("${booking.children.young-discount-percent}")
    private BigDecimal youngChildDiscountPercent;
    @Value("${booking.children.teen-max-age}")
    private Integer teenChildMaxAge;
    @Value("${booking.children.teen-discount-percent}")
    private BigDecimal teenChildDiscountPercent;
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
    public BigDecimal getBookingTotalPrice(Long days,
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

    public BigDecimal calculateBookingRefund(Long bookingId) {

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        return calculateRefund(
                booking.getTotalPrice(),
                booking.getCheckIn(),
                "Booking has already started"
        );
    }

    public BigDecimal calculateTicketRefund(Long flightBookingId) {

        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        return calculateRefund(
                flightBooking.getTotalPrice(),
                flightBooking.getFlight().getDepartureTime(),
                "Flight has already departed"
        );
    }

    private BigDecimal calculatePassengerPrice(
            PassengerRequest passenger,
            FlightEntity flight,
            SeatEntity seat,
            FareBaggageEntity baggagePolicy
    ) {

        BigDecimal baseFare = calculateBaseFare(
                passenger.getAge()
        );

        BigDecimal seatPrice = calculateSeatPrice(seat);

        BigDecimal baggagePrice = calculateBaggagePrice(
                baggagePolicy
        );

        return baseFare
                .add(seatPrice)
                .add(baggagePrice);
    }

    public BigDecimal calculateBaseFare(
            Integer age
    ) {

        BigDecimal basePrice = BigDecimal.ZERO;

        if (age <= youngChildMaxAge) {
            return BigDecimal.ZERO;
        }

        if (age <= teenChildMaxAge) {
            BigDecimal discountPercent = teenChildDiscountPercent;

            return basePrice
                    .multiply(
                            BigDecimal.valueOf(100)
                                    .subtract(discountPercent)
                    )
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return basePrice;
    }

    public BigDecimal calculateFlightTotalPrice(
            TicketRequest request,
            FlightEntity flight
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (PassengerRequest passenger : request.getPassengers()) {

            SeatEntity seat = seatRepository.findById(passenger.getSeatId())
                    .orElseThrow(() ->
                            new NotFoundException("Seat not found"));

            FareBaggageEntity policy = fareBaggageRepository
                    .findByAirlineAndTicketClass(
                            flight.getAirline(),
                            seat.getTicketClass()
                    ).orElseThrow(() -> new NotFoundException("Fare baggage policy not found"));

            BigDecimal passengerPrice = calculatePassengerPrice(
                    passenger,
                    flight,
                    seat,
                    policy
            );

            total = total.add(passengerPrice);
        }

        return total;
    }

    private BigDecimal calculateSeatPrice(SeatEntity seat) {
        return seat.getPrice();
    }

    private BigDecimal calculateBaggagePrice(FareBaggageEntity baggagePolicy) {
        return baggagePolicy.getPrice();
    }
}
