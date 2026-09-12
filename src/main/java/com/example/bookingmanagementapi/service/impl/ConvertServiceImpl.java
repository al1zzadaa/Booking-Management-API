package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.service.ConvertService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ConvertServiceImpl implements ConvertService {
    @Value("${currency.usd-to-azn}")
    private BigDecimal usdToAzn;

    @Value("${currency.usd-to-try}")
    private BigDecimal usdToTry;

    @Value("${currency.usd-to-rub}")
    private BigDecimal usdToRub;

    @Value("${currency.eur-to-usd}")
    private BigDecimal eurToUsd;

    @Override
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {

        if (from == to) {
            return amount;
        }

        // Convert to USD
        BigDecimal amountInUsd = switch (from) {
            case USD -> amount;

            case AZN -> amount.divide(
                    usdToAzn,
                    2,
                    RoundingMode.HALF_UP);

            case EUR -> amount.multiply(
                    eurToUsd
            ).setScale(2, RoundingMode.HALF_UP);

            case TR -> amount.divide(
                    usdToTry,
                    2,
                    RoundingMode.HALF_UP);

            case RUB -> amount.divide(
                    usdToRub,
                    2,
                    RoundingMode.HALF_UP);
        };


        // Convert from USD
        return switch (to) {
            case USD -> amountInUsd;

            case AZN -> amountInUsd.multiply(
                    usdToAzn
            ).setScale(2, RoundingMode.HALF_UP);

            case EUR -> amountInUsd.divide(
                    eurToUsd,
                    2,
                    RoundingMode.HALF_UP);

            case TR -> amountInUsd.multiply(
                    usdToTry
            ).setScale(2, RoundingMode.HALF_UP);

            case RUB -> amountInUsd.multiply(
                    usdToRub
            ).setScale(2, RoundingMode.HALF_UP);
        };
    }
}
