package com.example.bookingmanagementapi.exception;

public class SeatNotAvailable extends RuntimeException {
    public SeatNotAvailable(String message) {
        super(message);
    }
}
