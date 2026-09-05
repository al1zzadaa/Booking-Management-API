package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHotelReviewRequest {

    @NotBlank
    @Size(min = 3, max = 1000)
    private String comment;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}
