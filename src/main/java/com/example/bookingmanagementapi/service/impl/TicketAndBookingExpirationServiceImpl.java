package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.BookingStatus;
import com.example.bookingmanagementapi.enums.TicketStatus;
import com.example.bookingmanagementapi.repository.BookingRepository;
import com.example.bookingmanagementapi.repository.FlightBookingRepository;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.TicketAndBookingExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketAndBookingExpirationServiceImpl implements TicketAndBookingExpirationService {

    private final FlightBookingRepository flightBookingRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Override
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

            notificationService.sendBookingCancellationNotification(
                    booking.getUser().getId()
            );
        }
    }

    @Override
    public void expireUnpaidHotelBookings() {
        List<BookingEntity> bookings =
                bookingRepository
                        .findAllByBookingStatusAfterAndPaymentDeadlineBefore(
                                BookingStatus.PENDING,
                                LocalDateTime.now()
                        );

        for (BookingEntity booking : bookings) {

            booking.setBookingStatus(BookingStatus.EXPIRED);

            notificationService.sendBookingExpirationNotification(
                    booking.getUser().getId()
            );
        }
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
    }

}
