package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.PassengerType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerRequest {

    @NotNull
    private PassengerType type;

    @NotNull
    @Positive
    private Integer age;

    @NotNull
    @Positive
    private Long seatId;
}