package com.example.bookingmanagementapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WithdrawRequest {
    @NotNull
    private Long accountId;
    private BigDecimal amount;
}
