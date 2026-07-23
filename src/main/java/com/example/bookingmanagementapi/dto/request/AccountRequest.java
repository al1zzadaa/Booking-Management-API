package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {
    private Long userId;
    private Currency currency;
}
