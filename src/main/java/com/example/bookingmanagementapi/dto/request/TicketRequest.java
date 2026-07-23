package com.example.bookingmanagementapi.dto.request;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class TicketRequest {
    private Long userId;
    private Long accountId;
    private Long flightId;
    private Long seatId;
}
