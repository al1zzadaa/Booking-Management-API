package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.PassengerRequest
import com.example.bookingmanagementapi.dto.request.TicketRequest
import com.example.bookingmanagementapi.entity.*
import com.example.bookingmanagementapi.enums.PassengerType
import com.example.bookingmanagementapi.enums.TicketClass
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.repository.BookingRepository
import com.example.bookingmanagementapi.repository.FareBaggageRepository
import com.example.bookingmanagementapi.repository.FlightBookingRepository
import com.example.bookingmanagementapi.repository.SeatRepository
import com.example.bookingmanagementapi.service.impl.CalculationServiceImpl
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.time.LocalDateTime

class CalculationServiceTest extends Specification {

    def bookingRepository = Mock(BookingRepository)
    def seatRepository = Mock(SeatRepository)
    def fareBaggageRepository = Mock(FareBaggageRepository)
    def flightBookingRepository = Mock(FlightBookingRepository)

    def calculationService = new CalculationServiceImpl(
            bookingRepository,
            seatRepository,
            fareBaggageRepository,
            flightBookingRepository
    )

    def setup() {
        ReflectionTestUtils.setField(
                calculationService,
                "youngChildMaxAge",
                5
        )

        ReflectionTestUtils.setField(
                calculationService,
                "youngChildDiscountPercent",
                new BigDecimal("50")
        )

        ReflectionTestUtils.setField(
                calculationService,
                "teenChildMaxAge",
                12
        )

        ReflectionTestUtils.setField(
                calculationService,
                "teenChildDiscountPercent",
                new BigDecimal("25")
        )

        ReflectionTestUtils.setField(
                calculationService,
                "percent10",
                10
        )

        ReflectionTestUtils.setField(
                calculationService,
                "percent20",
                20
        )

        ReflectionTestUtils.setField(
                calculationService,
                "percent30",
                30
        )

        ReflectionTestUtils.setField(
                calculationService,
                "percent50",
                50
        )

        ReflectionTestUtils.setField(
                calculationService,
                "percent70",
                70
        )

        ReflectionTestUtils.setField(
                calculationService,
                "infantMaxAge",
                2
        )

        ReflectionTestUtils.setField(
                calculationService,
                "infantDiscountPercent",
                new BigDecimal("100")
        )
    }

    def "getBigDecimal should apply 10 percent cancellation fee when 30 or more days are left"() {
        when:
        def result = calculationService.getBigDecimal(
                30,
                new BigDecimal("100")
        )

        then:
        result == new BigDecimal("10.00")
    }

    def "getBigDecimal should apply 20 percent cancellation fee when 15 to 29 days are left"() {
        when:
        def result = calculationService.getBigDecimal(
                20,
                new BigDecimal("100")
        )

        then:
        result == new BigDecimal("20.00")
    }

    def "getBigDecimal should apply 30 percent cancellation fee when 7 to 14 days are left"() {
        when:
        def result = calculationService.getBigDecimal(
                10,
                new BigDecimal("100")
        )

        then:
        result == new BigDecimal("30.00")
    }

    def "getBigDecimal should apply 50 percent cancellation fee when 3 to 6 days are left"() {
        when:
        def result = calculationService.getBigDecimal(
                5,
                new BigDecimal("100")
        )

        then:
        result == new BigDecimal("50.00")
    }

    def "getBigDecimal should apply 70 percent cancellation fee when less than 3 days are left"() {
        when:
        def result = calculationService.getBigDecimal(
                2,
                new BigDecimal("100")
        )

        then:
        result == new BigDecimal("70.00")
    }

    def "calculateRefund should return refund minus cancellation fee"() {
        given:
        def eventDate = LocalDateTime.now().plusDays(31)

        when:
        def result = calculationService.calculateRefund(
                new BigDecimal("100"),
                eventDate,
                "Already started"
        )

        then:
        result == new BigDecimal("90.00")

    }

    def "calculateRefund should throw exception when event has already started"() {
        given:
        def eventDate = LocalDateTime.now().minusDays(1)

        when:
        calculationService.calculateRefund(
                new BigDecimal("100"),
                eventDate,
                "Already started"
        )

        then:
        def exception = thrown(IllegalStateException)
        exception.message == "Already started"
    }

    def "getBookingTotalPrice should calculate total with adults and children"() {
        given:
        def room = new RoomEntity()
        room.setAdultPrice(new BigDecimal("100"))
        room.setPricePerNight(new BigDecimal("50"))
        room.setChildDiscountPercent(new BigDecimal("20"))

        def childrenAges = [
                1,   // infant -> 100% discount -> 0
                5,   // young child -> 50% discount -> 50
                10,  // teen -> 25% discount -> 75
                15   // room discount -> 20% -> 80
        ]

        when:
        def result = calculationService.getBookingTotalPrice(
                3L,
                2,
                childrenAges,
                room
        )

        then:
        // Room: 50 * 3 = 150
        // Adults: 100 * 2 = 200
        // Children: 0 + 50 + 75 + 80 = 205
        // Total = 555
        result == new BigDecimal("555.00")
    }

    def "getBookingTotalPrice should calculate total without children"() {
        given:
        def room = new RoomEntity()
        room.setAdultPrice(new BigDecimal("100"))
        room.setPricePerNight(new BigDecimal("50"))
        room.setChildDiscountPercent(new BigDecimal("20"))

        when:
        def result = calculationService.getBookingTotalPrice(
                2L,
                2,
                [],
                room
        )

        then:
        // 50 * 2 + 100 * 2 = 300
        result == new BigDecimal("300")
    }

    def "calculateBookingRefund should calculate refund successfully"() {
        given:
        def booking = new BookingEntity()
        booking.setTotalPrice(new BigDecimal("100"))
        booking.setCheckIn(LocalDateTime.now().plusDays(31))

        when:
        def result = calculationService.calculateBookingRefund(1L)

        then:
        1 * bookingRepository.findById(1L) >> Optional.of(booking)

        result == new BigDecimal("90.00")
    }

    def "calculateBookingRefund should throw exception when booking is not found"() {
        when:
        calculationService.calculateBookingRefund(1L)

        then:
        1 * bookingRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Booking not found"
    }

    def "calculateBookingRefund should throw exception when booking has already started"() {
        given:
        def booking = new BookingEntity()
        booking.setTotalPrice(new BigDecimal("100"))
        booking.setCheckIn(LocalDateTime.now().minusDays(1))

        when:
        calculationService.calculateBookingRefund(1L)

        then:
        1 * bookingRepository.findById(1L) >> Optional.of(booking)

        def exception = thrown(IllegalStateException)
        exception.message == "Booking has already started"
    }

    def "calculateTicketRefund should calculate refund successfully"() {
        given:
        def flight = new FlightEntity()
        flight.setDepartureTime(LocalDateTime.now().plusDays(31))

        def flightBooking = new FlightBookingEntity()
        flightBooking.setTotalPrice(new BigDecimal("200"))
        flightBooking.setFlight(flight)

        when:
        def result = calculationService.calculateTicketRefund(1L)

        then:
        1 * flightBookingRepository.findById(1L) >> Optional.of(flightBooking)

        result == new BigDecimal("180.00")
    }

    def "calculateTicketRefund should throw exception when flight booking is not found"() {
        when:
        calculationService.calculateTicketRefund(1L)

        then:
        1 * flightBookingRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Flight booking not found"
    }

    def "calculateTicketRefund should throw exception when flight has already departed"() {
        given:
        def flight = new FlightEntity()
        flight.setDepartureTime(LocalDateTime.now().minusDays(1))

        def flightBooking = new FlightBookingEntity()
        flightBooking.setTotalPrice(new BigDecimal("200"))
        flightBooking.setFlight(flight)

        when:
        calculationService.calculateTicketRefund(1L)

        then:
        1 * flightBookingRepository.findById(1L) >> Optional.of(flightBooking)

        def exception = thrown(IllegalStateException)
        exception.message == "Flight has already departed"
    }

    def "calculateBaseFare should return zero for young child"() {
        when:
        def result = calculationService.calculateBaseFare(5)

        then:
        result == BigDecimal.ZERO
    }

    def "calculateBaseFare should return zero for teen child"() {
        when:
        def result = calculationService.calculateBaseFare(10)

        then:
        result == BigDecimal.ZERO
    }

    def "calculateBaseFare should return zero for adult"() {
        when:
        def result = calculationService.calculateBaseFare(25)

        then:
        result == BigDecimal.ZERO
    }

    def "calculateFlightTotalPrice should calculate total for all passengers"() {
        given:
        def airline = new AirlineEntity()

        def flight = new FlightEntity()
        flight.setAirline(airline)

        def seat1 = new SeatEntity()
        seat1.setId(1L)
        seat1.setTicketClass(TicketClass.ECONOMY)
        seat1.setPrice(new BigDecimal("20"))

        def seat2 = new SeatEntity()
        seat2.setId(2L)
        seat2.setTicketClass(TicketClass.BUSINESS)
        seat2.setPrice(new BigDecimal("50"))

        def baggage1 = new FareBaggageEntity()
        baggage1.setPrice(new BigDecimal("10"))

        def baggage2 = new FareBaggageEntity()
        baggage2.setPrice(new BigDecimal("30"))

        def passenger1 = new PassengerRequest()
        passenger1.setType(PassengerType.CHILD)
        passenger1.setAge(5)
        passenger1.setSeatId(1L)

        def passenger2 = new PassengerRequest()
        passenger2.setType(PassengerType.ADULT)
        passenger2.setAge(30)
        passenger2.setSeatId(2L)

        def request = new TicketRequest()
        request.setAccountId(1L)
        request.setFlightId(10L)
        request.setPassengers([
                passenger1,
                passenger2
        ])

        when:
        def result = calculationService.calculateFlightTotalPrice(
                request,
                flight
        )

        then:
        1 * seatRepository.findById(1L) >> Optional.of(seat1)
        1 * seatRepository.findById(2L) >> Optional.of(seat2)

        1 * fareBaggageRepository.findByAirlineAndTicketClass(
                airline,
                TicketClass.ECONOMY
        ) >> Optional.of(baggage1)

        1 * fareBaggageRepository.findByAirlineAndTicketClass(
                airline,
                TicketClass.BUSINESS
        ) >> Optional.of(baggage2)

        // Both passengers have base fare = 0
        // Passenger 1: 0 + 20 + 10 = 30
        // Passenger 2: 0 + 50 + 30 = 80
        // Total = 110
        result == new BigDecimal("110")
    }

    def "calculateFlightTotalPrice should throw exception when seat is not found"() {
        given:
        def passenger = new PassengerRequest()
        passenger.setType(PassengerType.ADULT)
        passenger.setAge(25)
        passenger.setSeatId(1L)

        def request = new TicketRequest()
        request.setAccountId(1L)
        request.setFlightId(10L)
        request.setPassengers([passenger])

        when:
        calculationService.calculateFlightTotalPrice(
                request,
                new FlightEntity()
        )

        then:
        1 * seatRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Seat not found"

        0 * fareBaggageRepository._
    }

    def "calculateFlightTotalPrice should throw exception when fare baggage policy is not found"() {
        given:
        def airline = new AirlineEntity()

        def flight = new FlightEntity()
        flight.setAirline(airline)

        def seat = new SeatEntity()
        seat.setId(1L)
        seat.setTicketClass(TicketClass.ECONOMY)
        seat.setPrice(new BigDecimal("20"))

        def passenger = new PassengerRequest()
        passenger.setType(PassengerType.ADULT)
        passenger.setAge(25)
        passenger.setSeatId(1L)

        def request = new TicketRequest()
        request.setAccountId(1L)
        request.setFlightId(10L)
        request.setPassengers([passenger])

        when:
        calculationService.calculateFlightTotalPrice(
                request,
                flight
        )

        then:
        1 * seatRepository.findById(1L) >> Optional.of(seat)

        1 * fareBaggageRepository.findByAirlineAndTicketClass(
                airline,
                TicketClass.ECONOMY
        ) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Fare baggage policy not found"
    }

    def "calculateFlightTotalPrice should return zero for empty passenger list"() {
        given:
        def request = new TicketRequest()
        request.setAccountId(1L)
        request.setFlightId(10L)
        request.setPassengers([])

        when:
        def result = calculationService.calculateFlightTotalPrice(
                request,
                new FlightEntity()
        )

        then:
        result == BigDecimal.ZERO

        0 * seatRepository._
        0 * fareBaggageRepository._
    }
}