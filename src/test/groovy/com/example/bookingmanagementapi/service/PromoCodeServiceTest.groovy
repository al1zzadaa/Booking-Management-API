package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.PromoCodeFilter
import com.example.bookingmanagementapi.dto.request.PromoCodeRequest
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse
import com.example.bookingmanagementapi.entity.PromoCodeEntity
import com.example.bookingmanagementapi.enums.DiscountType
import com.example.bookingmanagementapi.exception.BadRequestException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.PromoCodeMapper
import com.example.bookingmanagementapi.repository.PromoCodeRepository
import com.example.bookingmanagementapi.service.impl.PromoCodeServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import spock.lang.Specification

import java.time.LocalDateTime

class PromoCodeServiceTest extends Specification {

    def promoCodeMapper = Mock(PromoCodeMapper)
    def promoCodeRepository = Mock(PromoCodeRepository)
    def validationUtil = Mock(ValidationUtil)

    def promoCodeService = new PromoCodeServiceImpl(
            promoCodeMapper,
            promoCodeRepository,
            validationUtil
    )


    def "create should validate time, map request and save promo code"() {
        given:
        def request = new PromoCodeRequest()
        request.setCode("WINTER")
        request.setDiscountType(DiscountType.FIXED_AMOUNT)
        request.setDiscountValue(15)
        request.setUsageLimit(10)
        request.setStartDate(LocalDateTime.of(2026, 9, 25, 10, 0))
        request.setEndDate(LocalDateTime.of(2026, 10, 25, 10, 0))

        def entity = new PromoCodeEntity()

        when:
        promoCodeService.create(request)

        then:
        1 * validationUtil.checkTime(
                request.getStartDate(),
                request.getEndDate()
        )
        1 * promoCodeMapper.toEntity(request) >> entity
        1 * promoCodeRepository.save(entity)
    }


    def "getById should return promo code response"() {
        given:
        def entity = new PromoCodeEntity()
        def response = new PromoCodeResponse()

        when:
        def result = promoCodeService.getById(10L)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.of(entity)
        1 * promoCodeMapper.toDto(entity) >> response

        result == response
    }


    def "getById should throw NotFoundException when promo code does not exist"() {
        given:
        // After fixing production code to use getPromoCodeEntity(id)

        when:
        promoCodeService.getById(10L)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.empty()

        thrown(NotFoundException)

        0 * promoCodeMapper._
    }


    def "getAll should return mapped promo codes"() {
        given:
        def filter = new PromoCodeFilter()
        def pageable = PageRequest.of(0, 10)

        def entity1 = new PromoCodeEntity()
        def entity2 = new PromoCodeEntity()

        def response1 = new PromoCodeResponse()
        def response2 = new PromoCodeResponse()

        def page = new PageImpl<PromoCodeEntity>(
                [entity1, entity2],
                pageable,
                2
        )

        when:
        def result = promoCodeService.getAll(filter, pageable)

        then:
        1 * promoCodeRepository.findAll(_, pageable) >> page
        1 * promoCodeMapper.toDto(entity1) >> response1
        1 * promoCodeMapper.toDto(entity2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
    }


    def "getAll should return empty page when no promo codes exist"() {
        given:
        def filter = new PromoCodeFilter()
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<PromoCodeEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = promoCodeService.getAll(filter, pageable)

        then:
        1 * promoCodeRepository.findAll(_, pageable) >> page
        0 * promoCodeMapper.toDto(_)

        result.empty
    }


    def "deleteById should delete existing promo code"() {
        given:
        def entity = new PromoCodeEntity()

        when:
        promoCodeService.deleteById(10L)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.of(entity)
        1 * promoCodeRepository.delete(entity)
    }


    def "deleteById should throw NotFoundException when promo code does not exist"() {
        when:
        promoCodeService.deleteById(10L)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.empty()

        thrown(NotFoundException)

        0 * promoCodeRepository.delete(_)
    }


    def "update should update and save promo code"() {
        given:
        def request = new PromoCodeRequest()
        def entity = new PromoCodeEntity()

        when:
        promoCodeService.update(10L, request)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.of(entity)
        1 * promoCodeMapper.update(request, entity)
        1 * promoCodeRepository.save(entity)
    }


    def "update should throw NotFoundException when promo code does not exist"() {
        given:
        def request = new PromoCodeRequest()

        when:
        promoCodeService.update(10L, request)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.empty()

        thrown(NotFoundException)

        0 * promoCodeMapper._
        0 * promoCodeRepository.save(_)
    }


    def "findByCode should return promo code response"() {
        given:
        def entity = new PromoCodeEntity()
        def response = new PromoCodeResponse()

        when:
        def result = promoCodeService.findByCode("SUMMER25")

        then:
        1 * promoCodeRepository.findByCode("SUMMER25") >> Optional.of(entity)
        1 * promoCodeMapper.toDto(entity) >> response

        result == response
    }


    def "findByCode should throw NotFoundException when code does not exist"() {
        when:
        promoCodeService.findByCode("UNKNOWN")

        then:
        1 * promoCodeRepository.findByCode("UNKNOWN") >> Optional.empty()

        thrown(NotFoundException)

        0 * promoCodeMapper._
    }


    def "isValid should return true when usage limit is not reached"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setUsedCount(5)
        entity.setUsageLimit(10)

        when:
        def result = promoCodeService.isValid("SUMMER25")

        then:
        1 * promoCodeRepository.findByCode("SUMMER25") >> Optional.of(entity)

        result
    }


    def "isValid should return false when usage limit is reached"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setUsedCount(10)
        entity.setUsageLimit(10)

        when:
        def result = promoCodeService.isValid("SUMMER25")

        then:
        1 * promoCodeRepository.findByCode("SUMMER25") >> Optional.of(entity)

        !result
    }


    def "activate should set promo code active and save it"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setId(10L)
        entity.setActive(false)

        when:
        promoCodeService.activate(10L)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.of(entity)
        1 * promoCodeRepository.save(entity)

        entity.getActive()
    }


    def "deactivate should set promo code inactive and save it"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setId(10L)
        entity.setActive(true)

        when:
        promoCodeService.deactivate(10L)

        then:
        1 * promoCodeRepository.findById(10L) >> Optional.of(entity)
        1 * promoCodeRepository.save(entity)

        !entity.getActive()
    }


    def "calculateFinalAmount should return amount when code is null"() {
        when:
        def result = promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                null
        )

        then:
        result == new BigDecimal("100.00")
        0 * promoCodeRepository._
    }


    def "calculateFinalAmount should return amount when code is blank"() {
        when:
        def result = promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "    "
        )

        then:
        result == new BigDecimal("100.00")
        0 * promoCodeRepository._
    }


    def "calculateFinalAmount should apply percentage discount"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(true)
        entity.setEndDate(LocalDateTime.now().plusDays(1))
        entity.setUsageLimit(100)
        entity.setUsedCount(20)
        entity.setDiscountType(DiscountType.PERCENTAGE)
        entity.setDiscountValue(new BigDecimal("20"))

        when:
        def result = promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "SAVE20"
        )

        then:
        1 * promoCodeRepository.findByCode("SAVE20") >> Optional.of(entity)

        result == new BigDecimal("80.00")
    }


    def "calculateFinalAmount should apply fixed discount"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(true)
        entity.setEndDate(LocalDateTime.now().plusDays(1))
        entity.setUsageLimit(100)
        entity.setUsedCount(20)
        entity.setDiscountType(DiscountType.FIXED_AMOUNT)
        entity.setDiscountValue(new BigDecimal("25"))

        when:
        def result = promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "SAVE25"
        )

        then:
        1 * promoCodeRepository.findByCode("SAVE25") >> Optional.of(entity)

        result == new BigDecimal("75.00")
    }


    def "calculateFinalAmount should not make amount negative for fixed discount"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(true)
        entity.setEndDate(LocalDateTime.now().plusDays(1))
        entity.setUsageLimit(100)
        entity.setUsedCount(20)
        entity.setDiscountType(DiscountType.FIXED_AMOUNT)
        entity.setDiscountValue(new BigDecimal("150"))

        when:
        def result = promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "SAVE150"
        )

        then:
        1 * promoCodeRepository.findByCode("SAVE150") >> Optional.of(entity)

        result == new BigDecimal("0.00")
    }


    def "calculateFinalAmount should throw BadRequestException for invalid percentage"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(true)
        entity.setEndDate(LocalDateTime.now().plusDays(1))
        entity.setUsedCount(0)
        entity.setUsageLimit(100)
        entity.setDiscountType(DiscountType.PERCENTAGE)
        entity.setDiscountValue(new BigDecimal("150"))

        when:
        promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "BAD150"
        )

        then:
        1 * promoCodeRepository.findByCode("BAD150") >> Optional.of(entity)

        thrown(BadRequestException)
    }


    def "calculateFinalAmount should throw BadRequestException when promo code is inactive"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(false)
        entity.setEndDate(LocalDateTime.now().plusDays(1))
        entity.setUsedCount(0)
        entity.setUsageLimit(100)

        when:
        promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "INACTIVE"
        )

        then:
        1 * promoCodeRepository.findByCode("INACTIVE") >> Optional.of(entity)

        thrown(BadRequestException)
    }


    def "calculateFinalAmount should throw BadRequestException when promo code is expired"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(true)
        entity.setEndDate(LocalDateTime.now().minusMinutes(1))
        entity.setUsedCount(0)
        entity.setUsageLimit(100)

        when:
        promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "EXPIRED"
        )

        then:
        1 * promoCodeRepository.findByCode("EXPIRED") >> Optional.of(entity)

        thrown(BadRequestException)
    }


    def "calculateFinalAmount should throw BadRequestException when usage limit is reached"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setActive(true)
        entity.setEndDate(LocalDateTime.now().plusDays(1))
        entity.setUsedCount(10)
        entity.setUsageLimit(10)

        when:
        promoCodeService.calculateFinalAmount(
                new BigDecimal("100.00"),
                "LIMIT"
        )

        then:
        1 * promoCodeRepository.findByCode("LIMIT") >> Optional.of(entity)

        thrown(BadRequestException)
    }


    def "markAsUsed should do nothing when code is null"() {
        when:
        promoCodeService.markAsUsed(null)

        then:
        0 * promoCodeRepository._
    }


    def "markAsUsed should do nothing when code is blank"() {
        when:
        promoCodeService.markAsUsed("   ")

        then:
        0 * promoCodeRepository._
    }


    def "markAsUsed should increment used count"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setUsedCount(4)
        entity.setUsageLimit(10)

        when:
        promoCodeService.markAsUsed("SAVE20")

        then:
        1 * promoCodeRepository.findByCode("SAVE20") >> Optional.of(entity)

        entity.getUsedCount() == 5
    }


    def "markAsUsed should throw BadRequestException when usage limit is exceeded"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setUsedCount(10)
        entity.setUsageLimit(10)

        when:
        promoCodeService.markAsUsed("SAVE20")

        then:
        1 * promoCodeRepository.findByCode("SAVE20") >> Optional.of(entity)

        thrown(BadRequestException)

        entity.getUsedCount() == 10
    }


    def "markAsUsed should increment used count when usage limit is null"() {
        given:
        def entity = new PromoCodeEntity()
        entity.setUsedCount(4)
        entity.setUsageLimit(null)

        when:
        promoCodeService.markAsUsed("UNLIMITED")

        then:
        1 * promoCodeRepository.findByCode("UNLIMITED") >> Optional.of(entity)

        entity.getUsedCount() == 5
    }
}