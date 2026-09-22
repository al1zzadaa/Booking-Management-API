package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.entity.PromoCodeEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.entity.UserPromoCodeEntity
import com.example.bookingmanagementapi.event.ApplyPromoCodeEvent
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.repository.PromoCodeRepository
import com.example.bookingmanagementapi.repository.UserPromoCodeRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.UserPromoCodeServiceImpl
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

import java.time.LocalDateTime

class UserPromoCodeServiceTest extends Specification {

    def userRepository = Mock(UserRepository)
    def promoCodeRepository = Mock(PromoCodeRepository)
    def userPromoCodeRepository = Mock(UserPromoCodeRepository)
    def eventPublisher = Mock(ApplicationEventPublisher)

    def service = new UserPromoCodeServiceImpl(
            userRepository,
            promoCodeRepository,
            userPromoCodeRepository,
            eventPublisher
    )

    def "should return without doing anything when promo code is null"() {
        when:
        service.applyPromoCode(1L, null)

        then:
        0 * _
    }

    def "should return without doing anything when promo code is blank"() {
        when:
        service.applyPromoCode(1L, "   ")

        then:
        0 * _
    }

    def "should apply promo code successfully"() {
        given:
        def user = new UserEntity()
        user.id = 1L

        def promoCode = new PromoCodeEntity()
        promoCode.id = 10L
        promoCode.code = "DISCOUNT10"

        when:
        service.applyPromoCode(1L, "DISCOUNT10")

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * promoCodeRepository.findByCode("DISCOUNT10") >> Optional.of(promoCode)

        1 * userPromoCodeRepository.save({
            it.user == user &&
                    it.promoCode == promoCode &&
                    it.usedAt != null
        })

        1 * eventPublisher.publishEvent({
            it instanceof ApplyPromoCodeEvent
        })
    }

    def "should throw when user does not exist"() {
        when:
        service.applyPromoCode(1L, "DISCOUNT10")

        then:
        1 * userRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Promo code not found"

        0 * promoCodeRepository._
        0 * userPromoCodeRepository._
        0 * eventPublisher._
    }

    def "should throw when promo code does not exist"() {
        given:
        def user = new UserEntity()
        user.id = 1L

        when:
        service.applyPromoCode(1L, "DISCOUNT10")

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * promoCodeRepository.findByCode("DISCOUNT10") >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Promo code not found"

        0 * userPromoCodeRepository._
        0 * eventPublisher._
    }

    def "should save user promo code with correct data"() {
        given:
        def user = new UserEntity()
        user.id = 1L

        def promoCode = new PromoCodeEntity()
        promoCode.id = 10L
        promoCode.code = "DISCOUNT10"

        when:
        service.applyPromoCode(1L, "DISCOUNT10")

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * promoCodeRepository.findByCode("DISCOUNT10") >> Optional.of(promoCode)

        1 * userPromoCodeRepository.save({
            it.user.id == 1L &&
                    it.promoCode.id == 10L &&
                    it.usedAt != null
        })

        1 * eventPublisher.publishEvent(_)
    }

    def "should publish apply promo code event with correct user id"() {
        given:
        def user = new UserEntity()
        user.id = 1L

        def promoCode = new PromoCodeEntity()
        promoCode.id = 10L
        promoCode.code = "DISCOUNT10"

        when:
        service.applyPromoCode(1L, "DISCOUNT10")

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * promoCodeRepository.findByCode("DISCOUNT10") >> Optional.of(promoCode)
        1 * userPromoCodeRepository.save(_)

        1 * eventPublisher.publishEvent({
            it instanceof ApplyPromoCodeEvent &&
                    it.userId() == 1L
        })
    }

    def "should return user promo codes"() {
        given:

        def user = new UserEntity()
        user.id = 1L

        def promoCode = new PromoCodeEntity()
        promoCode.id = 10L

        def userPromoCode = UserPromoCodeEntity.builder()
                .id(100L)
                .user(user)
                .promoCode(promoCode)
                .usedAt(LocalDateTime.of(2026, 9, 20, 12, 30))
                .build()

        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<>(
                [userPromoCode],
                pageable,
                1
        )

        when:
        def before = LocalDateTime.now()
        def result = service.getUserPromoCodes(1L, pageable)
        def after = LocalDateTime.now()

        then:
        1 * userRepository.existsById(1L) >> true
        1 * userPromoCodeRepository.findAllByUserId(1L, pageable) >> page

        result.content.size() == 1

        result.content[0].id == 100L
        result.content[0].userId == 1L
        result.content[0].planId == 10L

        result.content[0].usedAt >= before
        result.content[0].usedAt <= after
    }

    def "should return empty page when user has no promo codes"() {
        given:
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<UserPromoCodeEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = service.getUserPromoCodes(1L, pageable)

        then:
        1 * userRepository.existsById(1L) >> true
        1 * userPromoCodeRepository.findAllByUserId(1L, pageable) >> page

        result.content.isEmpty()
        result.totalElements == 0
    }

    def "should throw when user does not exist while getting promo codes"() {
        given:
        def pageable = PageRequest.of(0, 10)

        when:
        service.getUserPromoCodes(1L, pageable)

        then:
        1 * userRepository.existsById(1L) >> false

        def exception = thrown(NotFoundException)
        exception.message == "User not found"

        0 * userPromoCodeRepository._
    }

    def "should pass pageable to repository when getting promo codes"() {
        given:
        def pageable = PageRequest.of(2, 20)

        def page = new PageImpl<UserPromoCodeEntity>(
                [],
                pageable,
                0
        )

        when:
        service.getUserPromoCodes(1L, pageable)

        then:
        1 * userRepository.existsById(1L) >> true
        1 * userPromoCodeRepository.findAllByUserId(1L, pageable) >> page
    }
}
