package com.example.bookingmanagementapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyPointRequest {
    private Long userId;
    private Integer points;
    private String description;

}
