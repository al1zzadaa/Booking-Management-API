package com.example.bookingmanagementapi.scheduler;

import com.example.bookingmanagementapi.service.TicketAndBookingExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final TicketAndBookingExpirationService ticketAndBookingExpirationService;

    @Scheduled(fixedRate = 60_000)
    public void expireUnpaidFlightBookings() {
        ticketAndBookingExpirationService.expireUnpaidFlightBookings();
    }

    @Scheduled(fixedRate = 60_000)
    public void expireBookings() {
        ticketAndBookingExpirationService.expireUnpaidHotelBookings();
    }

    @Scheduled(fixedDelay = 60_000)
    public void updateBookingStatuses() {
        ticketAndBookingExpirationService.updateBookingStatuses();
    }
}
