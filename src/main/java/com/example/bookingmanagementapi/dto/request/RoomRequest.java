package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Rooms;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {

    @NotNull
    @Positive
    private Long hotelId;

    @NotNull
    @Positive
    private Integer roomNumber;

    @NotBlank
    @Size(max = 100)
    private String roomName;

    @NotNull
    private Rooms roomType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal pricePerNight;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer roomCapacity;

    @NotBlank
    @Size(max = 2000)
    private String description;
}
