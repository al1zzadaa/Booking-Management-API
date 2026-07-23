package com.example.bookingmanagementapi.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AccountFilter {
    private Long userId;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;
    private String currency;
    private Boolean active;
    private LocalDate createdFrom;
    private LocalDate createdTo;

}
