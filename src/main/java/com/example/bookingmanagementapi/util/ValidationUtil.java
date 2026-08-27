package com.example.bookingmanagementapi.util;

import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.Tickets;
import com.example.bookingmanagementapi.exception.AccessDeniedException;
import com.example.bookingmanagementapi.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ValidationUtil {

    public void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id is null or 0");
        }
    }

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

}
