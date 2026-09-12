package com.example.bookingmanagementapi.util;

import com.example.bookingmanagementapi.dto.request.PassengerRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.enums.PassengerType;
import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.TicketStatus;
import com.example.bookingmanagementapi.enums.Tickets;
import com.example.bookingmanagementapi.exception.AccessDeniedException;
import com.example.bookingmanagementapi.exception.BadRequestException;
import com.example.bookingmanagementapi.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidationUtil {

    public void validateSeatNumber(Integer seatNumber) {
        if (seatNumber == null ||  seatNumber < 1 || seatNumber > 20) {
            throw new ValidationException("seatNumber is null or wrong");
        }
    }

    public void validateSeatRow(Rows seatRow) {
        if (seatRow == null ) {
            throw new ValidationException("seatRow is null or empty");
        }
    }

    public void validateTicketClass(Tickets ticketClass) {
        if (ticketClass == null) {
            throw new ValidationException("ticketClass is null or empty");
        }
    }

    public void validateRating(Integer rating) {
        if (rating == null ||  rating < 1 || rating > 5) {
            throw new ValidationException("rating is null or wrong");
        }
    }

    public void validateRoomNo(Integer roomNo) {
        if (roomNo == null || roomNo < 1) {
            throw new ValidationException("roomNo is null or empty");
        }
    }

    public void checkUserIdEqualsToUsedUsedId(Long userId, Long usedId) {
        if (!userId.equals(usedId)) {
            throw new AccessDeniedException("This not belongs to user");
        }
    }

    public void validateTicketCanBePaid(TicketEntity ticket) {
        if (ticket.getStatus() != TicketStatus.RESERVED) {
            throw new IllegalStateException(
                    "Ticket cannot be paid. Current status: " + ticket.getStatus()
            );
        }
    }

    public void validateRequestSeats(TicketRequest ticketRequest) {
        Set<Long> seatIds = ticketRequest.getPassengers()
                .stream()
                .map(PassengerRequest::getSeatId)
                .collect(Collectors.toSet());

        if (seatIds.size() != ticketRequest.getPassengers().size()) {
            throw new ValidationException("Duplicate seat selected");
        }
    }

    public void validatePassengerAges(TicketRequest ticketRequest) {
        for (PassengerRequest passenger : ticketRequest.getPassengers()) {

            if (passenger.getAge() < 0) {
                throw new ValidationException("Age cannot be negative");
            }

            if (passenger.getType() == PassengerType.INFANT
                    && passenger.getAge() >= 2) {
                throw new ValidationException(
                        "Infant passenger must be under 2"
                );
            }

            if (passenger.getType() == PassengerType.ADULT
                    && passenger.getAge() < 18) {
                throw new ValidationException(
                        "Adult passenger must be 18 or older"
                );
            }

            if (passenger.getType() == PassengerType.CHILD
                    && passenger.getAge() >= 18) {
                throw new ValidationException(
                        "Child passenger must be under 18"
                );
            }
        }
    }

    public void checkTime(LocalDateTime departureTime, LocalDateTime arrivalTime) {
        if(!departureTime.isBefore(arrivalTime)) {
            throw new BadRequestException("Error in time validation 'before-after' ");
        }
    }
}
