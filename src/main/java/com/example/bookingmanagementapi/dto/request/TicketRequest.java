package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TicketRequest {

    @NotNull
    @Positive
    private Long accountId;

    @NotNull
    @Positive
    private Long flightId;

    @NotEmpty
    @Valid
    private List<PassengerRequest> passengers;
}
