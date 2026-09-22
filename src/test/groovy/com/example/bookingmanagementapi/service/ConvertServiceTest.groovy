package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.enums.Currency
import com.example.bookingmanagementapi.service.impl.ConvertServiceImpl
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class ConvertServiceTest extends Specification {

    @Subject
    def convertService = new ConvertServiceImpl()

    def setup() {
        ReflectionTestUtils.setField(
                convertService,
                "usdToAzn",
                new BigDecimal("1.70")
        )

        ReflectionTestUtils.setField(
                convertService,
                "usdToTry",
                new BigDecimal("43")
        )

        ReflectionTestUtils.setField(
                convertService,
                "usdToRub",
                new BigDecimal("80")
        )

        ReflectionTestUtils.setField(
                convertService,
                "eurToUsd",
                new BigDecimal("1.14")
        )
    }

    def "convert should return same amount when currencies are the same"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.USD,
                Currency.USD
        )

        then:
        result == new BigDecimal("100")
    }

    def "convert should convert USD to AZN"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.USD,
                Currency.AZN
        )

        then:
        result == new BigDecimal("170.00")
    }

    def "convert should convert USD to TRY"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.USD,
                Currency.TR
        )

        then:
        result == new BigDecimal("4300.00")
    }

    def "convert should convert USD to RUB"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.USD,
                Currency.RUB
        )

        then:
        result == new BigDecimal("8000.00")
    }

    def "convert should convert USD to EUR"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.USD,
                Currency.EUR
        )

        then:
        result == new BigDecimal("87.72")
    }

    def "convert should convert AZN to USD"() {
        when:
        def result = convertService.convert(
                new BigDecimal("170"),
                Currency.AZN,
                Currency.USD
        )

        then:
        result == new BigDecimal("100.00")
    }

    def "convert should convert TRY to USD"() {
        when:
        def result = convertService.convert(
                new BigDecimal("4300"),
                Currency.TR,
                Currency.USD
        )

        then:
        result == new BigDecimal("100.00")
    }

    def "convert should convert RUB to USD"() {
        when:
        def result = convertService.convert(
                new BigDecimal("8000"),
                Currency.RUB,
                Currency.USD
        )

        then:
        result == new BigDecimal("100.00")
    }

    def "convert should convert EUR to USD"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.EUR,
                Currency.USD
        )

        then:
        result == new BigDecimal("114.00")
    }

    def "convert should convert AZN to EUR"() {
        when:
        def result = convertService.convert(
                new BigDecimal("170"),
                Currency.AZN,
                Currency.EUR
        )

        then:
        result == new BigDecimal("87.72")
    }

    def "convert should convert TRY to AZN"() {
        when:
        def result = convertService.convert(
                new BigDecimal("4300"),
                Currency.TR,
                Currency.AZN
        )

        then:
        result == new BigDecimal("170.00")
    }

    def "convert should convert RUB to AZN"() {
        when:
        def result = convertService.convert(
                new BigDecimal("8000"),
                Currency.RUB,
                Currency.AZN
        )

        then:
        result == new BigDecimal("170.00")
    }

    def "convert should round converted amount to two decimal places"() {
        when:
        def result = convertService.convert(
                new BigDecimal("100"),
                Currency.AZN,
                Currency.USD
        )

        then:
        result == new BigDecimal("58.82")
    }
}