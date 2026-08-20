package com.example.bookingmanagementapi.dto.filter;

import com.example.bookingmanagementapi.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeFilter {
    private String code;
    private BigDecimal minDiscountValue;
    private BigDecimal maxDiscountValue;
    private DiscountType discountType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minUsageLimit;
    private Integer maxUsageLimit;
    private Integer usedCount;
    private Boolean active;
}
