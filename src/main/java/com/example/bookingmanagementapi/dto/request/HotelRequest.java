package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelRequest {

    @NotBlank
    @Size(max = 100)
    private String hotelName;

    @NotBlank
    @Size(max = 100)
    private String country;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotBlank
    @Size(max = 255)
    private String hotelAddress;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer stars;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotNull
    @PositiveOrZero
    private Integer distanceToSea;
}
