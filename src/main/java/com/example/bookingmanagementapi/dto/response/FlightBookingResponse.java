package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightBookingResponse {
    private Long id;
    private UserEntity user;
    private AccountEntity account;
    private FlightEntity flight;
    private List<TicketEntity> tickets;
    private LocalDateTime paymentDeadline;
    private BigDecimal totalPrice;
    private TicketStatus status;
}
