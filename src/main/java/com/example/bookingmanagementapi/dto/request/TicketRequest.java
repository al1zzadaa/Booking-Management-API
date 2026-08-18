package com.example.bookingmanagementapi.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class TicketRequest {
    private Long accountId;
    private Long flightId;
    private List<PassengerRequest> passengers;
}
