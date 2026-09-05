package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Tickets;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FareBaggageRequest {

    @NotNull
    @Positive
    private Long airlineId;

    @NotNull
    private Tickets ticket;

    @NotNull
    @Min(0)
    private Integer baggage;

    @NotNull
    @Positive
    private Integer handLuggage;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}
