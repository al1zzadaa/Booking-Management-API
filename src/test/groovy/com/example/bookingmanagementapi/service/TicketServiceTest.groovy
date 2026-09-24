package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.TicketFilter
import com.example.bookingmanagementapi.dto.request.PassengerRequest
import com.example.bookingmanagementapi.dto.request.PaymentRequest
import com.example.bookingmanagementapi.dto.request.TicketRequest
import com.example.bookingmanagementapi.dto.response.FlightBookingResponse
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse
import com.example.bookingmanagementapi.entity.*
import com.example.bookingmanagementapi.enums.FlightStatus
import com.example.bookingmanagementapi.enums.TicketStatus
import com.example.bookingmanagementapi.enums.TicketClass
import com.example.bookingmanagementapi.event.BookingPaymentEvent
import com.example.bookingmanagementapi.exception.*
import com.example.bookingmanagementapi.mapper.FlightBookingMapper
import com.example.bookingmanagementapi.mapper.TicketMapper
import com.example.bookingmanagementapi.repository.*
import com.example.bookingmanagementapi.service.impl.TicketServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

import java.time.LocalDateTime

class TicketServiceTest extends Specification {

    def ticketRepository = Mock(TicketRepository)
    def flightRepository = Mock(FlightRepository)
    def seatRepository = Mock(SeatRepository)
    def ticketMapper = Mock(TicketMapper)
    def fareBaggageRepository = Mock(FareBaggageRepository)
    def validationUtil = Mock(ValidationUtil)
    def userRepository = Mock(UserRepository)
    def userService = Mock(UserService)
    def transactionService = Mock(TransactionService)
    def accountRepository = Mock(AccountRepository)
    def notificationService = Mock(NotificationService)
    def loyaltyPointService = Mock(LoyaltyPointService)
    def eventPublisher = Mock(ApplicationEventPublisher)
    def flightBookingRepository = Mock(FlightBookingRepository)
    def accountService = Mock(AccountService)
    def flightBookingMapper = Mock(FlightBookingMapper)
    def calculationService = Mock(CalculationService)

    def ticketService = new TicketServiceImpl(
            ticketRepository,
            flightRepository,
            seatRepository,
            ticketMapper,
            fareBaggageRepository,
            validationUtil,
            userRepository,
            userService,
            transactionService,
            accountRepository,
            notificationService,
            loyaltyPointService,
            eventPublisher,
            flightBookingRepository,
            accountService,
            flightBookingMapper,
            calculationService
    )

    def "book should create reserved booking successfully"() {
        given:
        def user = new UserEntity()
        user.setId(1L)
        user.setEmail("john@gmail.com")

        def account = new AccountEntity()
        account.setId(10L)
        account.setUser(user)

        def airline = new AirlineEntity()
        airline.setId(40L)

        def flight = new FlightEntity()
        flight.setId(20L)
        flight.setAirline(airline)
        flight.setStatus(FlightStatus.SCHEDULED)
        flight.setDepartureTime(
                LocalDateTime.of(2026, 10, 20, 15, 0)
        )

        def seat = new SeatEntity()
        seat.setId(30L)
        seat.setFlight(flight)
        seat.setTicketClass(TicketClass.ECONOMY)
        seat.setIsAvailable(true)

        def policy = new FareBaggageEntity()
        policy.setPrice(new BigDecimal("30"))

        def passenger = new PassengerRequest()
        passenger.setSeatId(30L)
        passenger.setAge(25)

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)
        request.setPassengers([passenger])

        def totalPrice = new BigDecimal("150")

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * validationUtil.validateRequestSeats(request)
        1 * validationUtil.validatePassengerAges(request)

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * flightRepository.findById(20L) >> Optional.of(flight)

        1 * seatRepository.findByIdForUpdate(30L) >> Optional.of(seat)

        1 * fareBaggageRepository.findByAirlineAndTicketClass(
                airline,
                TicketClass.ECONOMY
        ) >> Optional.of(policy)

        1 * calculationService.calculateFlightTotalPrice(
                request,
                flight
        ) >> totalPrice

        1 * flightBookingRepository.save({
            it.user == user &&
                    it.account == account &&
                    it.flight == flight &&
                    it.status == TicketStatus.RESERVED &&
                    it.totalPrice == totalPrice &&
                    it.tickets.size() == 1 &&
                    it.paymentDeadline != null
        })

        1 * notificationService.sendBookingNotification(1L)

        seat.getIsAvailable() == false
    }


    def "book should throw NotFoundException when user does not exist"() {
        given:
        def request = new TicketRequest()

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * validationUtil.validateRequestSeats(request)
        1 * validationUtil.validatePassengerAges(request)

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "User not found"

        0 * accountRepository._
        0 * flightRepository._
    }


    def "book should throw NotFoundException when account does not exist"() {
        given:
        def user = new UserEntity()
        user.setId(1L)

        def request = new TicketRequest()
        request.setAccountId(10L)

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * validationUtil.validateRequestSeats(request)
        1 * validationUtil.validatePassengerAges(request)

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Account not found"

        0 * flightRepository._
    }


    def "book should throw FlightException when flight is not scheduled"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def flight = new FlightEntity(id: 20L)
        flight.setStatus(FlightStatus.CANCELLED)

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * flightRepository.findById(20L) >> Optional.of(flight)

        def exception = thrown(FlightException)
        exception.message == "Flight is not available for booking"

        0 * seatRepository._
        0 * flightBookingRepository._
    }


    def "book should throw FlightException when flight has already departed"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def flight = new FlightEntity(id: 20L)
        flight.setStatus(FlightStatus.SCHEDULED)
        flight.setDepartureTime(LocalDateTime.now().minusMinutes(1))

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * flightRepository.findById(20L) >> Optional.of(flight)

        def exception = thrown(FlightException)
        exception.message == "Flight has already departed"

        0 * seatRepository._
    }


    def "book should throw ValidationException when account does not belong to user"() {
        given:
        def user = new UserEntity(id: 1L)

        def anotherUser = new UserEntity(id: 2L)

        def account = new AccountEntity(id: 10L)
        account.setUser(anotherUser)

        def flight = new FlightEntity(id: 20L)
        flight.setStatus(FlightStatus.SCHEDULED)
        flight.setDepartureTime(LocalDateTime.now().plusDays(1))

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * flightRepository.findById(20L) >> Optional.of(flight)

        def exception = thrown(ValidationException)
        exception.message == "account not owned by user"

        0 * seatRepository._
    }


    def "book should throw NotFoundException when seat does not exist"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def flight = new FlightEntity(id: 20L)
        flight.setStatus(FlightStatus.SCHEDULED)
        flight.setDepartureTime(LocalDateTime.now().plusDays(1))

        def passenger = new PassengerRequest()
        passenger.setSeatId(30L)

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)
        request.setPassengers([passenger])

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)
        1 * flightRepository.findById(20L) >> Optional.of(flight)

        1 * seatRepository.findByIdForUpdate(30L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Seat not found"

        0 * flightBookingRepository.save(_)
    }


    def "book should throw ValidationException when seat belongs to another flight"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def flight = new FlightEntity(id: 20L)
        flight.setStatus(FlightStatus.SCHEDULED)
        flight.setDepartureTime(LocalDateTime.now().plusDays(1))

        def anotherFlight = new FlightEntity(id: 99L)

        def seat = new SeatEntity(id: 30L)
        seat.setFlight(anotherFlight)
        seat.setIsAvailable(true)

        def passenger = new PassengerRequest()
        passenger.setSeatId(30L)

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)
        request.setPassengers([passenger])

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)
        1 * flightRepository.findById(20L) >> Optional.of(flight)
        1 * seatRepository.findByIdForUpdate(30L) >> Optional.of(seat)

        def exception = thrown(ValidationException)
        exception.message == "Flight does not have this seat"

        0 * flightBookingRepository.save(_)
    }


    def "book should throw SeatNotAvailable when seat is unavailable"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def flight = new FlightEntity(id: 20L)
        flight.setStatus(FlightStatus.SCHEDULED)
        flight.setDepartureTime(LocalDateTime.now().plusDays(1))

        def seat = new SeatEntity(id: 30L)
        seat.setFlight(flight)
        seat.setIsAvailable(false)

        def passenger = new PassengerRequest()
        passenger.setSeatId(30L)

        def request = new TicketRequest()
        request.setAccountId(10L)
        request.setFlightId(20L)
        request.setPassengers([passenger])

        when:
        ticketService.book(request, "john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)
        1 * flightRepository.findById(20L) >> Optional.of(flight)
        1 * seatRepository.findByIdForUpdate(30L) >> Optional.of(seat)

        def exception = thrown(SeatNotAvailable)
        exception.message == "Seat 30 is not available"

        0 * flightBookingRepository.save(_)
    }

    def "payTicket should pay and confirm booking successfully"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking = new FlightBookingEntity()
        booking.setId(50L)
        booking.setUser(user)

        def ticket = new TicketEntity()
        ticket.setPrice(new BigDecimal("200"))
        ticket.setStatus(TicketStatus.RESERVED)

        booking.setTickets([ticket])

        def request = new PaymentRequest()

        when:
        ticketService.payTicket(
                "john@gmail.com",
                50L,
                request
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.of(booking)

        1 * transactionService.payForTickets(
                50L,
                request
        )

        1 * validationUtil.validateTicketCanBePaid(ticket)

        1 * loyaltyPointService.earnPoints(
                1L,
                new BigDecimal("200"),
                "Points earned from ticket payment"
        )

        1 * eventPublisher.publishEvent({
            it instanceof BookingPaymentEvent &&
                    it.userId() == 1L
        })

        ticket.getStatus() == TicketStatus.CONFIRMED
        booking.getStatus() == TicketStatus.CONFIRMED
    }


    def "payTicket should throw NotFoundException when user does not exist"() {
        given:
        def request = new PaymentRequest()

        when:
        ticketService.payTicket(
                "john@gmail.com",
                50L,
                request
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "User not found"

        0 * flightBookingRepository._
    }


    def "payTicket should throw NotFoundException when flight booking does not exist"() {
        given:
        def user = new UserEntity(id: 1L)
        def request = new PaymentRequest()

        when:
        ticketService.payTicket(
                "john@gmail.com",
                50L,
                request
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Flight booking not found"

        0 * transactionService._
    }


    def "payTicket should throw ValidationException when booking belongs to another user"() {
        given:
        def user = new UserEntity(id: 1L)
        def anotherUser = new UserEntity(id: 2L)

        def booking = new FlightBookingEntity()
        booking.setUser(anotherUser)

        def request = new PaymentRequest()

        when:
        ticketService.payTicket(
                "john@gmail.com",
                50L,
                request
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.of(booking)

        def exception = thrown(ValidationException)
        exception.message == "Flight booking does not belong to user"

        0 * transactionService._
        0 * loyaltyPointService._
        0 * eventPublisher._
    }

    def "cancel should refund and cancel confirmed booking"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking = new FlightBookingEntity()
        booking.setId(50L)
        booking.setUser(user)
        booking.setStatus(TicketStatus.CONFIRMED)

        def seat = new SeatEntity()
        seat.setIsAvailable(false)

        def ticket = new TicketEntity()
        ticket.setStatus(TicketStatus.CONFIRMED)
        ticket.setSeat(seat)

        booking.setTickets([ticket])

        def refund = new BigDecimal("180")

        when:
        ticketService.cancel(
                "john@gmail.com",
                50L
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.of(booking)

        1 * calculationService.calculateTicketRefund(50L) >> refund

        1 * transactionService.refundTicket(
                booking,
                refund
        )

        1 * notificationService.sendTicketCancellationNotification(1L)

        booking.getStatus() == TicketStatus.CANCELLED
        ticket.getStatus() == TicketStatus.CANCELLED
        seat.getIsAvailable() == true
    }


    def "cancel should throw NotFoundException when booking does not exist"() {
        given:
        def user = new UserEntity()
        user.setId(1L)
        user.setEmail("john@gmail.com")

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.empty()

        when:
        ticketService.cancel(
                user.getEmail(),
                50L
        )

        then:
        def exception = thrown(NotFoundException)
        exception.message == "Flight booking not found"

        0 * transactionService._
    }


    def "cancel should throw AccessDeniedException when booking belongs to another user"() {
        given:
        def user = new UserEntity(id: 1L)
        def anotherUser = new UserEntity(id: 2L)

        def booking = new FlightBookingEntity()
        booking.setUser(anotherUser)

        when:
        ticketService.cancel(
                "john@gmail.com",
                50L
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.of(booking)

        def exception = thrown(AccessDeniedException)
        exception.message == "Account does not belong to user"

        0 * calculationService._
        0 * transactionService._
    }


    def "cancel should throw ValidationException when ticket is not paid"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking = new FlightBookingEntity()
        booking.setUser(user)

        def ticket = new TicketEntity()
        ticket.setStatus(TicketStatus.RESERVED)

        booking.setTickets([ticket])

        when:
        ticketService.cancel(
                "john@gmail.com",
                50L
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * flightBookingRepository.findById(50L) >> Optional.of(booking)

        def exception = thrown(ValidationException)
        exception.message == "ticket not paid"

        0 * calculationService._
        0 * transactionService._
    }

    def "findAll should return mapped tickets"() {
        given:
        def filter = new TicketFilter()

        def ticket1 = new TicketEntity()
        def ticket2 = new TicketEntity()

        def response1 = new TicketResponse()
        def response2 = new TicketResponse()

        when:
        def result = ticketService.findAll(filter)

        then:
        1 * ticketRepository.findAll(_) >> [ticket1, ticket2]

        1 * ticketMapper.toListDto(
                [ticket1, ticket2]
        ) >> [response1, response2]

        result == [response1, response2]
    }


    def "findAll should return empty list when no tickets exist"() {
        given:
        def filter = new TicketFilter()

        when:
        def result = ticketService.findAll(filter)

        then:
        1 * ticketRepository.findAll(_) >> []
        1 * ticketMapper.toListDto([]) >> []

        result == []
    }

    def "findById should return ticket response"() {
        given:
        def ticket = new TicketEntity()
        def response = new TicketResponse()

        when:
        def result = ticketService.findById(50L)

        then:
        1 * ticketRepository.findById(50L) >> Optional.of(ticket)
        1 * ticketMapper.toDto(ticket) >> response

        result == response
    }


    def "findById should throw NotFoundException when ticket does not exist"() {
        when:
        ticketService.findById(50L)

        then:
        1 * ticketRepository.findById(50L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "ticket not found"

        0 * ticketMapper._
    }

    def "deleteTicketById should delete ticket when it exists"() {
        when:
        ticketService.deleteTicketById(50L)

        then:
        1 * ticketRepository.existsById(50L) >> true
        1 * ticketRepository.deleteById(50L)
    }


    def "deleteTicketById should throw NotFoundException when ticket does not exist"() {
        when:
        ticketService.deleteTicketById(50L)

        then:
        1 * ticketRepository.existsById(50L) >> false

        def exception = thrown(NotFoundException)
        exception.message == "ticket not found"

        0 * ticketRepository.deleteById(_)
    }

    def "getUserTickets should return mapped user tickets"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking1 = new FlightBookingEntity()
        def booking2 = new FlightBookingEntity()

        def response1 = new FlightBookingResponse()
        def response2 = new FlightBookingResponse()

        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<FlightBookingEntity>(
                [booking1, booking2],
                pageable,
                2
        )

        when:
        def result = ticketService.getUserTickets(
                "john@gmail.com",
                pageable
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)

        1 * flightBookingRepository.findAllByUserId(
                1L,
                pageable
        ) >> page

        1 * flightBookingMapper.toDto(booking1) >> response1
        1 * flightBookingMapper.toDto(booking2) >> response2

        result.content == [response1, response2]
    }


    def "getUserTickets should return empty page when user has no tickets"() {
        given:
        def user = new UserEntity(id: 1L)

        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<FlightBookingEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = ticketService.getUserTickets(
                "john@gmail.com",
                pageable
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)

        1 * flightBookingRepository.findAllByUserId(
                1L,
                pageable
        ) >> page

        0 * flightBookingMapper._

        result.empty
    }

    def "getUserTickets should throw NotFoundException when user does not exist"() {
        given:
        def pageable = PageRequest.of(0, 10)

        when:
        ticketService.getUserTickets(
                "john@gmail.com",
                pageable
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "User not found"

        0 * flightBookingRepository._
    }
}