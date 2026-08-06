package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.enums.LoyaltyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoyaltyPointResponse {
    private Long userId;
    private Integer points;
    private LoyaltyType type;
    private String description;
}
