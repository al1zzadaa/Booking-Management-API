package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String code;

    @NotNull
    @DecimalMin("1")
    private BigDecimal discountValue;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Future
    private LocalDateTime startDate;

    @NotNull
    @Future
    private LocalDateTime endDate;

    @NotNull
    @Positive
    private Integer usageLimit;
}
