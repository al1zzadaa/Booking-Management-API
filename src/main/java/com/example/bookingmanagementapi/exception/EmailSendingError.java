package com.example.bookingmanagementapi.exception;

public class EmailSendingError extends RuntimeException{
    public EmailSendingError(String message){
        super(message);
    }
}
