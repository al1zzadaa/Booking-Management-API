package com.example.bookingmanagementapi.service;

public interface TicketAndBookingExpirationService {

    void expireUnpaidFlightBookings();

    void expireUnpaidHotelBookings();

    void updateBookingStatuses();

    void updateFlightStatuses();
}
