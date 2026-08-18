package com.example.bookingmanagementapi.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserPromoCodeResponse {
    private Long id;
    private Long userId;
    private Long planId;
    private LocalDateTime usedAt;
}
