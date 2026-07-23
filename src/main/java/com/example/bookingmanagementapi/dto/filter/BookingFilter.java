package com.example.bookingmanagementapi.dto.filter;

import com.example.bookingmanagementapi.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingFilter {
    private Long userId;
    private Long hotelId;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BookingStatus status;
}
