package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Flights;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FlightRequest {

    @NotBlank
    @Size(max = 100)
    private String departureCountry;

    @NotBlank
    @Size(max = 100)
    private String departureCity;

    @NotBlank
    @Size(max = 100)
    private String arrivalCountry;

    @NotBlank
    @Size(max = 100)
    private String arrivalCity;

    @NotNull
    @Future
    private LocalDateTime departureTime;

    @NotNull
    @Future
    private LocalDateTime arrivalTime;

    @NotNull
    @Positive
    private Long airlineId;

    @NotNull
    @Positive
    private Long flightNumber;

    @NotNull
    private Flights status;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal basePrice;
}
