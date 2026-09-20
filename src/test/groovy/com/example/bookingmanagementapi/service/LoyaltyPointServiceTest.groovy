package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse
import com.example.bookingmanagementapi.entity.AccountEntity
import com.example.bookingmanagementapi.entity.LoyaltyPointEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.Currency
import com.example.bookingmanagementapi.enums.LoyaltyType
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.exception.ValidationException
import com.example.bookingmanagementapi.mapper.LoyaltyPointMapper
import com.example.bookingmanagementapi.repository.AccountRepository
import com.example.bookingmanagementapi.repository.LoyaltyPointRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.LoyaltyPointServiceImpl
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.math.BigDecimal

class LoyaltyPointServiceTest extends Specification {

    def loyaltyPointRepository = Mock(LoyaltyPointRepository)
    def loyaltyPointMapper = Mock(LoyaltyPointMapper)
    def accountRepository = Mock(AccountRepository)
    def userRepository = Mock(UserRepository)
    def convertService = Mock(ConvertService)

    def loyaltyPointService = new LoyaltyPointServiceImpl(
            loyaltyPointRepository,
            loyaltyPointMapper,
            accountRepository,
            userRepository,
            convertService
    )

    def setup() {
        ReflectionTestUtils.setField(
                loyaltyPointService,
                "earningRate",
                new BigDecimal("0.05")
        )

        ReflectionTestUtils.setField(
                loyaltyPointService,
                "pointValue",
                new BigDecimal("0.01")
        )
    }


    def "addPoints should save earned loyalty points successfully"() {
        given:
        def request = LoyaltyPointRequest.builder()
                .userId(10L)
                .points(100)
                .description("Booking reward")
                .build()

        def user = new UserEntity()
        user.setId(10L)

        when:
        loyaltyPointService.addPoints(request)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)

        1 * loyaltyPointRepository.save({
            it.user == user
            it.points == 100
            it.description == "Booking reward"
            it.type == LoyaltyType.EARN

            true
        })
    }


    def "addPoints should throw NotFoundException when user does not exist"() {
        given:
        def request = LoyaltyPointRequest.builder()
                .userId(10L)
                .points(100)
                .description("Booking reward")
                .build()

        when:
        loyaltyPointService.addPoints(request)

        then:
        1 * userRepository.findById(10L) >> Optional.empty()

        0 * loyaltyPointRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "User not found"
    }


    def "removePoints should save removed loyalty points successfully"() {
        given:
        def request = LoyaltyPointRequest.builder()
                .userId(10L)
                .points(50)
                .description("Cancelled booking")
                .build()

        def user = new UserEntity()
        user.setId(10L)

        when:
        loyaltyPointService.removePoints(request)

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)

        1 * loyaltyPointRepository.save({
            it.user == user
            it.points == 50
            it.description == "Cancelled booking"
            it.type == LoyaltyType.REMOVE

            true
        })
    }


    def "removePoints should throw NotFoundException when user does not exist"() {
        given:
        def request = LoyaltyPointRequest.builder()
                .userId(10L)
                .points(50)
                .description("Cancelled booking")
                .build()

        when:
        loyaltyPointService.removePoints(request)

        then:
        1 * userRepository.findById(10L) >> Optional.empty()

        0 * loyaltyPointRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "User not found"
    }


    def "getPoints should calculate available points correctly"() {
        given:
        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.EARN
        ) >> 500

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.USE
        ) >> 150

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.REFUND
        ) >> 50

        when:
        def result = loyaltyPointService.getPoints(10L)

        then:
        result == 400
    }


    def "getPoints should return zero when no points exist"() {
        given:
        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.EARN
        ) >> 0

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.USE
        ) >> 0

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.REFUND
        ) >> 0

        when:
        def result = loyaltyPointService.getPoints(10L)

        then:
        result == 0
    }


    def "getHistory should return loyalty point history"() {
        given:
        def entity1 = new LoyaltyPointEntity()
        def entity2 = new LoyaltyPointEntity()

        def response1 = new LoyaltyPointResponse()
        def response2 = new LoyaltyPointResponse()

        def entities = [entity1, entity2]
        def responses = [response1, response2]

        when:
        def result = loyaltyPointService.getHistory(10L)

        then:
        1 * loyaltyPointRepository.findAllByUserId(10L) >> entities

        1 * loyaltyPointMapper.toListDto(entities) >> responses

        result == responses
    }


    def "getHistory should return empty list when no history exists"() {
        given:
        def entities = []
        def responses = []

        when:
        def result = loyaltyPointService.getHistory(10L)

        then:
        1 * loyaltyPointRepository.findAllByUserId(10L) >> entities

        1 * loyaltyPointMapper.toListDto(entities) >> responses

        result == []
    }


    def "calculateEarnedPoints should calculate points correctly"() {
        given:
        def amount = new BigDecimal("100.00")

        when:
        def result = loyaltyPointService.calculateEarnedPoints(amount)

        then:
        result == 5
    }


    def "calculateEarnedPoints should round down decimal points"() {
        given:
        def amount = new BigDecimal("99.99")

        when:
        def result = loyaltyPointService.calculateEarnedPoints(amount)

        then:
        result == 4
    }


    def "earnPoints should calculate points and add them successfully"() {
        given:
        def amount = new BigDecimal("100.00")

        when:
        loyaltyPointService.earnPoints(
                10L,
                amount,
                "Booking reward"
        )

        then:
        1 * userRepository.findById(10L) >> Optional.of({
            def user = new UserEntity()
            user.setId(10L)
            user
        }())

        1 * loyaltyPointRepository.save({
            it.user.id == 10L
            it.points == 5
            it.description == "Booking reward"
            it.type == LoyaltyType.EARN

            true
        })
    }


    def "cancelPoints should convert amount and remove calculated points"() {
        given:
        def account = new AccountEntity()
        account.setId(20L)
        account.setCurrency(Currency.AZN)

        def user = new UserEntity()
        user.setId(10L)

        1 * accountRepository.findById(20L) >> Optional.of(account)

        1 * convertService.convert(
                new BigDecimal("100.00"),
                Currency.USD,
                Currency.AZN
        ) >> new BigDecimal("170.00")

        when:
        loyaltyPointService.cancelPoints(
                account,
                10L,
                new BigDecimal("100.00"),
                "Cancelled booking"
        )

        then:
        1 * userRepository.findById(10L) >> Optional.of(user)

        1 * loyaltyPointRepository.save({
            it.user == user
            it.points == 8
            it.description == "Cancelled booking"
            it.type == LoyaltyType.REMOVE

            true
        })
    }


    def "cancelPoints should throw NotFoundException when account does not exist"() {
        given:
        def account = new AccountEntity()
        account.setId(20L)

        when:
        loyaltyPointService.cancelPoints(
                account,
                10L,
                new BigDecimal("100.00"),
                "Cancelled booking"
        )

        then:
        1 * accountRepository.findById(20L) >> Optional.empty()

        0 * convertService._
        0 * userRepository._
        0 * loyaltyPointRepository._

        def exception = thrown(NotFoundException)
        exception.message == "Account not found"
    }


    def "usePoints should save used points successfully"() {
        given:
        def user = new UserEntity()
        user.setId(10L)
        user.setEmail("john@gmail.com")

        def account = new AccountEntity()
        account.setId(20L)
        account.setUser(user)

        when:
        loyaltyPointService.usePoints(
                20L,
                100,
                "Used for booking"
        )

        then:
        1 * accountRepository.findById(20L) >> Optional.of(account)

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.EARN
        ) >> 500

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.USE
        ) >> 100

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.REFUND
        ) >> 0

        1 * loyaltyPointRepository.save({
            it.user == user
            it.points == 100
            it.type == LoyaltyType.USE
            it.description == "Used for booking"

            true
        })
    }


    def "usePoints should throw ValidationException when points are null"() {
        when:
        loyaltyPointService.usePoints(
                20L,
                null,
                "Used for booking"
        )

        then:
        0 * accountRepository._
        0 * loyaltyPointRepository._

        def exception = thrown(ValidationException)
        exception.message == "Points must be greater than zero"
    }


    def "usePoints should throw ValidationException when points are zero"() {
        when:
        loyaltyPointService.usePoints(
                20L,
                0,
                "Used for booking"
        )

        then:
        0 * accountRepository._
        0 * loyaltyPointRepository._

        def exception = thrown(ValidationException)
        exception.message == "Points must be greater than zero"
    }


    def "usePoints should throw ValidationException when points are negative"() {
        when:
        loyaltyPointService.usePoints(
                20L,
                -10,
                "Used for booking"
        )

        then:
        0 * accountRepository._
        0 * loyaltyPointRepository._

        def exception = thrown(ValidationException)
        exception.message == "Points must be greater than zero"
    }


    def "usePoints should throw NotFoundException when account does not exist"() {
        when:
        loyaltyPointService.usePoints(
                20L,
                100,
                "Used for booking"
        )

        then:
        1 * accountRepository.findById(20L) >> Optional.empty()

        0 * loyaltyPointRepository.sumByUserAndType(_, _)
        0 * loyaltyPointRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "Account not found"
    }


    def "usePoints should throw ValidationException when user does not have enough points"() {
        given:
        def user = new UserEntity()
        user.setId(10L)

        def account = new AccountEntity()
        account.setId(20L)
        account.setUser(user)

        when:
        loyaltyPointService.usePoints(
                20L,
                100,
                "Used for booking"
        )

        then:
        1 * accountRepository.findById(20L) >> Optional.of(account)

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.EARN
        ) >> 50

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.USE
        ) >> 30

        1 * loyaltyPointRepository.sumByUserAndType(
                10L,
                LoyaltyType.REFUND
        ) >> 0

        0 * loyaltyPointRepository.save(_)

        def exception = thrown(ValidationException)
        exception.message == "Not enough loyalty points"
    }


    def "pointValue should return correct monetary value"() {
        when:
        def result = loyaltyPointService.pointValue(100)

        then:
        result == new BigDecimal("1.00")
    }


    def "pointValue should return zero when points are null"() {
        expect:
        loyaltyPointService.pointValue(null) == BigDecimal.ZERO
    }


    def "pointValue should return zero when points are zero"() {
        expect:
        loyaltyPointService.pointValue(0) == BigDecimal.ZERO
    }


    def "pointValue should return zero when points are negative"() {
        expect:
        loyaltyPointService.pointValue(-10) == BigDecimal.ZERO
    }
}