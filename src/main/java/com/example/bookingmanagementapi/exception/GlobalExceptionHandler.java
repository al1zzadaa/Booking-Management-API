package com.example.bookingmanagementapi.exception;

import com.example.bookingmanagementapi.dto.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(Exception e) {
        e.printStackTrace();
        return new ExceptionResponse("INTERNAL_SERVER_ERROR");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleException(NotFoundException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleException(ValidationException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleException(BadRequestException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleException(AccessDeniedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(AdminException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(AdminException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(AccountBlockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleException(AccountBlockedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(AccountDeletedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleException(AccountDeletedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(UserBlockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleException(UserBlockedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(UserDeletedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleException(UserDeletedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(UserNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleException(UserNotVerifiedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleException(InvalidTokenException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleException(TokenExpiredException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(FlightException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(FlightException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(FlightScheduleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(FlightScheduleException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(HotelException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(HotelException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(RoomException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(RoomException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(SeatNotAvailable.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(SeatNotAvailable e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(CapacityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(CapacityException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(DuplicateEntityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(DuplicateEntityException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(InsufficientBalanceException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(PaymentAlreadyCompletedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleException(PaymentAlreadyCompletedException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


    @ExceptionHandler(EmailSendingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(EmailSendingException e) {
        e.printStackTrace();
        return new ExceptionResponse(e.getMessage());
    }


}
