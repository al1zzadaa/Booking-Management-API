package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeResponse {
    private Long id;
    private String code;
    private BigDecimal discountValue;
    private DiscountType discountType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean active;
}
