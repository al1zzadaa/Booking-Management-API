package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.DepositRequest
import com.example.bookingmanagementapi.dto.request.PaymentRequest
import com.example.bookingmanagementapi.dto.request.SubscriptionRequest
import com.example.bookingmanagementapi.dto.request.WithdrawRequest
import com.example.bookingmanagementapi.dto.response.TransactionResponse
import com.example.bookingmanagementapi.entity.*
import com.example.bookingmanagementapi.enums.Currency
import com.example.bookingmanagementapi.enums.PaymentStatus
import com.example.bookingmanagementapi.enums.ReferenceType
import com.example.bookingmanagementapi.enums.TransactionType
import com.example.bookingmanagementapi.exception.*
import com.example.bookingmanagementapi.mapper.TransactionMapper
import com.example.bookingmanagementapi.repository.AccountRepository
import com.example.bookingmanagementapi.repository.BookingRepository
import com.example.bookingmanagementapi.repository.FlightBookingRepository
import com.example.bookingmanagementapi.repository.TransactionRepository
import com.example.bookingmanagementapi.service.impl.TransactionServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class TransactionServiceTest extends Specification {

    def transactionRepository = Mock(TransactionRepository)
    def transactionMapper = Mock(TransactionMapper)
    def accountRepository = Mock(AccountRepository)
    def convert = Mock(ConvertService)
    def promoCodeService = Mock(PromoCodeService)
    def bookingRepository = Mock(BookingRepository)
    def userPromoCodeService = Mock(UserPromoCodeService)
    def flightBookingRepository = Mock(FlightBookingRepository)
    def loyaltyPointService = Mock(LoyaltyPointService)

    def transactionService = new TransactionServiceImpl(
            transactionRepository,
            transactionMapper,
            accountRepository,
            convert,
            promoCodeService,
            bookingRepository,
            userPromoCodeService,
            flightBookingRepository,
            loyaltyPointService
    )


    def "delete transaction successfully"() {
        given:
        def transaction = new TransactionEntity(id: 1L)

        1 * transactionRepository.findById(1L) >> Optional.of(transaction)
        1 * transactionRepository.delete(transaction)

        when:
        transactionService.deleteTransaction(1L)

        then:
        noExceptionThrown()
    }


    def "get transaction by id successfully"() {
        given:
        def transaction = new TransactionEntity(id: 1L)
        def response = new TransactionResponse()

        1 * transactionRepository.findById(1L) >> Optional.of(transaction)
        1 * transactionMapper.toDto(transaction) >> response

        when:
        def result = transactionService.getTransactionById(1L)

        then:
        result == response
    }


    def "get transactions by user id successfully"() {
        given:
        def account = new AccountEntity(id: 1L)
        def transaction1 = new TransactionEntity(id: 1L, account: account)
        def transaction2 = new TransactionEntity(id: 2L, account: account)

        def pageable = PageRequest.of(0, 10)
        def page = new PageImpl<TransactionEntity>(
                [transaction1, transaction2],
                pageable,
                2
        )

        def response1 = new TransactionResponse()
        def response2 = new TransactionResponse()

        1 * transactionRepository.findById(1L) >>
                Optional.of(transaction1)

        1 * transactionRepository.findAllByAccount(account, pageable) >>
                page

        1 * transactionMapper.toDto(transaction1) >> response1
        1 * transactionMapper.toDto(transaction2) >> response2

        when:
        def result = transactionService.getTransactionsByUserId(1L, pageable)

        then:
        result.content == [response1, response2]
        result.totalElements == 2
    }


    def "pay for tickets successfully"() {
        given:
        def user = new UserEntity(id: 1L)
        def account = new AccountEntity(
                id: 10L,
                user: user,
                currency: Currency.AZN,
                balance: 500G
        )

        def ticket1 = new TicketEntity(price: 100G)
        def ticket2 = new TicketEntity(price: 50G)

        def flightBooking = new FlightBookingEntity(
                id: 20L,
                user: user,
                account: account,
                tickets: [ticket1, ticket2]
        )

        def request = new PaymentRequest(
                accountId: 10L,
                loyaltyPointsToUse: 0
        )

        1 * flightBookingRepository.findById(20L) >>
                Optional.of(flightBooking)

        1 * transactionRepository.existsByReferenceIdAndReferenceTypeAndType(
                20L,
                ReferenceType.FLIGHT_TICKET,
                TransactionType.PAYMENT
        ) >> false

        1 * loyaltyPointService.pointValue(0) >> 0G

        1 * accountRepository.findByIdForUpdate(10L) >>
                Optional.of(account)

        1 * promoCodeService.calculateFinalAmount(
                150G,
                null
        ) >> 150G

        1 * convert.convert(
                150G,
                Currency.USD,
                Currency.AZN
        ) >> 255G

        1 * promoCodeService.markAsUsed(null)

        1 * flightBookingRepository.save(flightBooking)

        1 * transactionRepository.save({
            it.amount == 255G
            it.amountInUsd == 150G
            it.currency == Currency.AZN
            it.account == account
            it.paymentStatus == PaymentStatus.PAID
            it.referenceType == ReferenceType.FLIGHT_TICKET
            it.type == TransactionType.PAYMENT
            it.referenceId == 20L
            it.description == "Payment for ticket"
        })

        1 * userPromoCodeService.applyPromoCode(1L, null)

        when:
        transactionService.payForTickets(20L, request)

        then:
        account.balance == 245G
        flightBooking.totalPrice == 150G
    }


    def "pay for tickets throws exception when booking does not exist"() {
        given:
        def request = new PaymentRequest(accountId: 10L)

        1 * flightBookingRepository.findById(20L) >> Optional.empty()

        when:
        transactionService.payForTickets(20L, request)

        then:
        thrown(NotFoundException)
        0 * accountRepository._
    }


    def "pay for tickets throws exception when booking belongs to another account"() {
        given:
        def account = new AccountEntity(id: 10L)
        def booking = new FlightBookingEntity(
                id: 20L,
                account: account
        )

        def request = new PaymentRequest(accountId: 99L)

        1 * flightBookingRepository.findById(20L) >>
                Optional.of(booking)

        when:
        transactionService.payForTickets(20L, request)

        then:
        thrown(AccessDeniedException)
        0 * transactionRepository._
    }


    def "pay for tickets throws exception when already paid"() {
        given:
        def account = new AccountEntity(id: 10L)
        def booking = new FlightBookingEntity(
                id: 20L,
                account: account
        )

        def request = new PaymentRequest(accountId: 10L)

        1 * flightBookingRepository.findById(20L) >>
                Optional.of(booking)

        1 * transactionRepository.existsByReferenceIdAndReferenceTypeAndType(
                20L,
                ReferenceType.FLIGHT_TICKET,
                TransactionType.PAYMENT
        ) >> true

        when:
        transactionService.payForTickets(20L, request)

        then:
        thrown(PaymentAlreadyCompletedException)
        0 * accountRepository._
    }


    def "pay for tickets throws exception when there are no tickets"() {
        given:
        def account = new AccountEntity(id: 10L)

        def booking = new FlightBookingEntity(
                id: 20L,
                account: account,
                tickets: []
        )

        def request = new PaymentRequest(accountId: 10L)

        1 * flightBookingRepository.findById(20L) >>
                Optional.of(booking)

        1 * transactionRepository.existsByReferenceIdAndReferenceTypeAndType(
                20L,
                ReferenceType.FLIGHT_TICKET,
                TransactionType.PAYMENT
        ) >> false

        when:
        transactionService.payForTickets(20L, request)

        then:
        thrown(ValidationException)
    }


    def "refund ticket successfully"() {
        given:
        def account = new AccountEntity(
                id: 10L,
                currency: Currency.AZN,
                balance: 100G
        )

        def booking = new FlightBookingEntity(
                id: 20L,
                account: account
        )

        1 * convert.convert(
                50G,
                Currency.USD,
                Currency.AZN
        ) >> 85G

        1 * transactionRepository.save({
            it.account == account
            it.amount == 85G
            it.amountInUsd == 50G
            it.currency == Currency.AZN
            it.type == TransactionType.REFUND
            it.referenceType == ReferenceType.FLIGHT_TICKET
            it.paymentStatus == PaymentStatus.REFUNDED
            it.referenceId == 20L
            it.description == "Refund"
        })

        when:
        transactionService.refundTicket(booking, 50G)

        then:
        account.balance == 185G
    }


    def "refund booking successfully"() {
        given:
        def account = new AccountEntity(
                id: 10L,
                currency: Currency.AZN,
                balance: 100G
        )

        def booking = new BookingEntity(
                id: 30L,
                account: account
        )

        1 * convert.convert(
                50G,
                Currency.USD,
                Currency.AZN
        ) >> 85G

        1 * transactionRepository.save({
            it.account == account
            it.amount == 85G
            it.amountInUsd == 50G
            it.currency == Currency.AZN
            it.type == TransactionType.REFUND
            it.referenceType == ReferenceType.HOTEL_BOOKING
            it.paymentStatus == PaymentStatus.REFUNDED
            it.referenceId == 30L
            it.description == "Refund"
        })

        when:
        transactionService.refundBooking(booking, 50G)

        then:
        account.balance == 185G
    }


    def "withdraw successfully"() {
        given:
        def account = new AccountEntity(
                id: 10L,
                currency: Currency.AZN,
                balance: 500G
        )

        def request = new WithdrawRequest(
                accountId: 10L,
                amount: 100G,
                currency: Currency.USD
        )

        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * convert.convert(
                100G,
                Currency.USD,
                Currency.AZN
        ) >> 170G

        1 * accountRepository.save(account)

        1 * transactionRepository.save({
            it.account == account
            it.amount == 100G
            it.type == TransactionType.WITHDRAW
            it.description == "Withdraw"
            it.referenceType == ReferenceType.ACCOUNT
            it.paymentStatus == PaymentStatus.PAID
            it.referenceId == 10L
        })

        when:
        transactionService.withdraw(request)

        then:
        account.balance == 330G
    }


    def "withdraw throws exception when account does not exist"() {
        given:
        def request = new WithdrawRequest(
                accountId: 10L,
                amount: 100G,
                currency: Currency.USD
        )

        1 * accountRepository.findById(10L) >> Optional.empty()

        when:
        transactionService.withdraw(request)

        then:
        thrown(NotFoundException)
        0 * convert._
        0 * transactionRepository._
    }


    def "withdraw throws exception when balance is insufficient"() {
        given:
        def account = new AccountEntity(
                id: 10L,
                currency: Currency.AZN,
                balance: 100G
        )

        def request = new WithdrawRequest(
                accountId: 10L,
                amount: 100G,
                currency: Currency.USD
        )

        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * convert.convert(
                100G,
                Currency.USD,
                Currency.AZN
        ) >> 170G

        when:
        transactionService.withdraw(request)

        then:
        thrown(InsufficientBalanceException)
        account.balance == 100G
        0 * accountRepository.save(_)
        0 * transactionRepository.save(_)
    }


    def "deposit successfully"() {
        given:
        def account = new AccountEntity(
                id: 10L,
                currency: Currency.AZN,
                balance: 100G
        )

        def request = new DepositRequest(
                accountId: 10L,
                amount: 100G,
                currency: Currency.USD
        )

        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * convert.convert(
                100G,
                Currency.USD,
                Currency.USD
        ) >> 100G

        1 * convert.convert(
                100G,
                Currency.USD,
                Currency.AZN
        ) >> 170G

        1 * accountRepository.save(account)

        1 * transactionRepository.save({
            it.account == account
            it.currency == Currency.AZN
            it.amountInUsd == 100G
            it.amount == 170G
            it.type == TransactionType.DEPOSIT
            it.description == "Deposit"
            it.referenceType == ReferenceType.ACCOUNT
            it.paymentStatus == PaymentStatus.PAID
            it.referenceId == 10L
        })

        when:
        transactionService.deposit(request)

        then:
        account.balance == 270G
    }


    def "deposit throws exception when account does not exist"() {
        given:
        def request = new DepositRequest(
                accountId: 10L,
                amount: 100G,
                currency: Currency.USD
        )

        1 * accountRepository.findById(10L) >> Optional.empty()

        when:
        transactionService.deposit(request)

        then:
        thrown(NotFoundException)
        0 * convert._
        0 * transactionRepository._
    }


    def "pay for booking successfully"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(
                id: 10L,
                user: user,
                currency: Currency.AZN,
                balance: 500G
        )

        def booking = new BookingEntity(
                id: 30L,
                user: user,
                account: account,
                totalPrice: 100G
        )

        def request = new PaymentRequest(
                accountId: 10L,
                loyaltyPointsToUse: 0
        )

        1 * transactionRepository.existsByReferenceIdAndReferenceTypeAndType(
                30L,
                ReferenceType.HOTEL_BOOKING,
                TransactionType.PAYMENT
        ) >> false

        1 * loyaltyPointService.pointValue(0) >> 0G

        1 * accountRepository.findByIdForUpdate(10L) >>
                Optional.of(account)

        1 * promoCodeService.calculateFinalAmount(
                100G,
                null
        ) >> 100G

        1 * convert.convert(
                100G,
                Currency.USD,
                Currency.AZN
        ) >> 170G

        1 * promoCodeService.markAsUsed(null)

        1 * bookingRepository.save(booking)

        1 * transactionRepository.save({
            it.amount == 170G
            it.amountInUsd == 100G
            it.currency == Currency.AZN
            it.account == account
            it.paymentStatus == PaymentStatus.PAID
            it.referenceType == ReferenceType.HOTEL_BOOKING
            it.type == TransactionType.PAYMENT
            it.referenceId == 30L
            it.description == "Payment for booking"
        })

        1 * userPromoCodeService.applyPromoCode(1L, null)

        when:
        transactionService.payForBooking(booking, request)

        then:
        account.balance == 330G
        booking.totalPrice == 100G
    }


    def "pay for booking throws exception when account does not belong to booking"() {
        given:
        def account = new AccountEntity(id: 10L)

        def booking = new BookingEntity(
                id: 30L,
                account: account
        )

        def request = new PaymentRequest(accountId: 99L)

        when:
        transactionService.payForBooking(booking, request)

        then:
        thrown(AccessDeniedException)
        0 * transactionRepository._
    }


    def "pay for booking throws exception when already paid"() {
        given:
        def account = new AccountEntity(id: 10L)

        def booking = new BookingEntity(
                id: 30L,
                account: account
        )

        def request = new PaymentRequest(accountId: 10L)

        1 * transactionRepository.existsByReferenceIdAndReferenceTypeAndType(
                30L,
                ReferenceType.HOTEL_BOOKING,
                TransactionType.PAYMENT
        ) >> true

        when:
        transactionService.payForBooking(booking, request)

        then:
        thrown(PaymentAlreadyCompletedException)
        0 * accountRepository._
    }


    def "process subscription payment successfully"() {
        given:
        def account = new AccountEntity(
                id: 10L,
                currency: Currency.AZN,
                balance: 500G
        )

        def request = new SubscriptionRequest(
                accountId: 10L
        )

        def plan = new SubscriptionPlanEntity(
                id: 5L,
                price: 100G
        )

        1 * accountRepository.findByIdForUpdate(10L) >>
                Optional.of(account)

        1 * promoCodeService.calculateFinalAmount(
                100G,
                null
        ) >> 100G

        1 * convert.convert(
                100G,
                Currency.USD,
                Currency.AZN
        ) >> 170G

        1 * promoCodeService.markAsUsed(null)

        1 * transactionRepository.save({
            it.amount == 170G
            it.amountInUsd == 100G
            it.currency == Currency.AZN
            it.account == account
            it.paymentStatus == PaymentStatus.PAID
            it.referenceType == ReferenceType.SUBSCRIPTION
            it.type == TransactionType.PAYMENT
            it.referenceId == 50L
            it.description == "Subscription payment"
        })

        when:
        transactionService.processSubscriptionPayment(
                request,
                50L,
                plan,
                "Subscription payment"
        )

        then:
        account.balance == 330G
    }


    def "process subscription payment throws exception when account does not exist"() {
        given:
        def request = new SubscriptionRequest(accountId: 10L)

        def plan = new SubscriptionPlanEntity(
                id: 5L,
                price: 100G
        )

        1 * accountRepository.findByIdForUpdate(10L) >> Optional.empty()

        when:
        transactionService.processSubscriptionPayment(
                request,
                50L,
                plan,
                "Subscription payment"
        )

        then:
        thrown(NotFoundException)
        0 * transactionRepository.save(_)
    }
}