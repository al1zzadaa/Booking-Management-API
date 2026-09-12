package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.entity.FlightBookingEntity;
import com.example.bookingmanagementapi.entity.SeatEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.enums.BookingStatus;
import com.example.bookingmanagementapi.enums.Flights;
import com.example.bookingmanagementapi.enums.TicketStatus;
import com.example.bookingmanagementapi.event.ExpireUnpaidEvent;
import com.example.bookingmanagementapi.repository.BookingRepository;
import com.example.bookingmanagementapi.repository.FlightBookingRepository;
import com.example.bookingmanagementapi.repository.FlightRepository;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.TicketAndBookingExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAndBookingExpirationServiceImpl implements TicketAndBookingExpirationService {

    private final FlightBookingRepository flightBookingRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void expireUnpaidFlightBookings() {

        List<FlightBookingEntity> bookings =
                flightBookingRepository
                        .findAllByStatusAndPaymentDeadlineBefore(
                                TicketStatus.RESERVED,
                                LocalDateTime.now()
                        );

        for (FlightBookingEntity booking : bookings) {

            booking.setStatus(TicketStatus.EXPIRED);

            for (TicketEntity ticket : booking.getTickets()) {

                SeatEntity seat = ticket.getSeat();
                seat.setIsAvailable(true);
                ticket.setStatus(TicketStatus.EXPIRED);
            }

            eventPublisher.publishEvent(new ExpireUnpaidEvent(booking.getUser().getId()));

        }

        log.info("Expired {} unpaid flight bookings", bookings.size());
    }

    @Override
    @Transactional
    public void expireUnpaidHotelBookings() {
        List<BookingEntity> bookings =
                bookingRepository
                        .findAllByBookingStatusAfterAndPaymentDeadlineBefore(
                                BookingStatus.PENDING,
                                LocalDateTime.now()
                        );

        for (BookingEntity booking : bookings) {

            booking.setBookingStatus(BookingStatus.EXPIRED);

            eventPublisher.publishEvent(new ExpireUnpaidEvent(booking.getUser().getId()));
        }

        log.info("Expired {} unpaid hotel bookings", bookings.size());
    }

    @Transactional
    @Override
    public void updateBookingStatuses() {

        LocalDateTime now = LocalDateTime.now();

        bookingRepository.updateCheckIns(
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                now
        );

        bookingRepository.updateCheckOuts(
                BookingStatus.CHECKED_IN,
                BookingStatus.CHECKED_OUT,
                now
        );

        log.info("Updated {} bookings", bookingRepository.count());
    }

    @Transactional
    @Override
    public void updateFlightStatuses() {

        LocalDateTime now = LocalDateTime.now();

        int startedFlights = flightRepository.startFlights(
                Flights.SCHEDULED,
                Flights.IN_PROGRESS,
                now
        );

        int landedFlights = flightRepository.landFlights(
                Flights.IN_PROGRESS,
                Flights.LANDED,
                now
        );

        int totalUpdated = startedFlights + landedFlights;

        log.info("Updated {} flights: {} started, {} landed",
                totalUpdated,
                startedFlights,
                landedFlights);
    }

}
