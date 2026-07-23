package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountRequest {
    private Long userId;
    private BigDecimal balance;
    private Currency currency;
}