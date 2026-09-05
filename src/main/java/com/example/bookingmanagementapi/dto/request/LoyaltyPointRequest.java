package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyPointRequest {

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Integer points;

    @NotBlank
    @Size(min = 3, max = 1000)
    private String description;
}
