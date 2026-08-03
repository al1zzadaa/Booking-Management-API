package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.enums.Currency;

import java.math.BigDecimal;

public interface ConvertService {

    BigDecimal convert(BigDecimal amount, Currency from, Currency to);
}
