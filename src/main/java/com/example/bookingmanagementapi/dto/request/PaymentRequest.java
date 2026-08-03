package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.enums.PaymentMethods;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    @NotNull
    private Long accountId;

    @NotNull
    private PaymentMethods paymentMethod;


    private String promoCode;
//
//    private Currency currency;
//    private BigDecimal amount;

}