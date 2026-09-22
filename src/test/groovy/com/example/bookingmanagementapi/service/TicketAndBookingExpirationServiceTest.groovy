package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.entity.*
import com.example.bookingmanagementapi.enums.BookingStatus
import com.example.bookingmanagementapi.enums.Flights
import com.example.bookingmanagementapi.enums.TicketStatus
import com.example.bookingmanagementapi.event.ExpireUnpaidEvent
import com.example.bookingmanagementapi.repository.BookingRepository
import com.example.bookingmanagementapi.repository.FlightBookingRepository
import com.example.bookingmanagementapi.repository.FlightRepository
import com.example.bookingmanagementapi.service.impl.TicketAndBookingExpirationServiceImpl
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification
import spock.lang.Subject

class TicketAndBookingExpirationServiceTest extends Specification {

    def flightBookingRepository = Mock(FlightBookingRepository)
    def bookingRepository = Mock(BookingRepository)
    def flightRepository = Mock(FlightRepository)
    def eventPublisher = Mock(ApplicationEventPublisher)


    def expirationService = new TicketAndBookingExpirationServiceImpl(
            flightBookingRepository,
            bookingRepository,
            flightRepository,
            eventPublisher
    )

    def "expireUnpaidFlightBookings should expire bookings and tickets and release seats"() {
        given:
        def user = new UserEntity()
        user.setId(1L)

        def seat1 = new SeatEntity()
        seat1.setIsAvailable(false)

        def seat2 = new SeatEntity()
        seat2.setIsAvailable(false)

        def ticket1 = new TicketEntity()
        ticket1.setSeat(seat1)
        ticket1.setStatus(TicketStatus.RESERVED)

        def ticket2 = new TicketEntity()
        ticket2.setSeat(seat2)
        ticket2.setStatus(TicketStatus.RESERVED)

        def booking = new FlightBookingEntity()
        booking.setUser(user)
        booking.setStatus(TicketStatus.RESERVED)
        booking.setTickets([ticket1, ticket2])

        when:
        expirationService.expireUnpaidFlightBookings()

        then:
        1 * flightBookingRepository.findAllByStatusAndPaymentDeadlineBefore(TicketStatus.RESERVED, _) >> [booking]

        1 * eventPublisher.publishEvent({
            it instanceof ExpireUnpaidEvent &&
                    it.userId() == 1L
        })

        booking.getStatus() == TicketStatus.EXPIRED

        ticket1.getStatus() == TicketStatus.EXPIRED
        ticket2.getStatus() == TicketStatus.EXPIRED

        seat1.getIsAvailable()
        seat2.getIsAvailable()
    }

    def "expireUnpaidFlightBookings should do nothing when there are no expired bookings"() {
        when:
        expirationService.expireUnpaidFlightBookings()

        then:
        1 * flightBookingRepository.findAllByStatusAndPaymentDeadlineBefore(TicketStatus.RESERVED, _) >> []

        0 * eventPublisher._
    }

    def "expireUnpaidHotelBookings should expire unpaid hotel bookings"() {
        given:
        def user = new UserEntity()
        user.setId(10L)

        def booking = new BookingEntity()
        booking.setUser(user)
        booking.setBookingStatus(BookingStatus.PENDING)

        when:
        expirationService.expireUnpaidHotelBookings()

        then:
        1 * bookingRepository.findAllByBookingStatusAfterAndPaymentDeadlineBefore(
                BookingStatus.PENDING,
                _
        ) >> [booking]

        1 * eventPublisher.publishEvent({
            it instanceof ExpireUnpaidEvent &&
                    it.userId == 10L
        })

        booking.getBookingStatus() == BookingStatus.EXPIRED
    }

    def "expireUnpaidHotelBookings should do nothing when there are no expired bookings"() {
        when:
        expirationService.expireUnpaidHotelBookings()

        then:
        1 * bookingRepository.findAllByBookingStatusAfterAndPaymentDeadlineBefore(
                BookingStatus.PENDING,
                _
        ) >> []

        0 * eventPublisher._
    }

    def "updateBookingStatuses should update check-in and check-out statuses"() {
        when:
        expirationService.updateBookingStatuses()

        then:
        1 * bookingRepository.updateCheckIns(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, _)

        1 * bookingRepository.updateCheckOuts(BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT, _)

        1 * bookingRepository.count()
    }

    def "updateFlightStatuses should update started and landed flights"() {
        when:
        expirationService.updateFlightStatuses()

        then:
        1 * flightRepository.startFlights(
                Flights.SCHEDULED,
                Flights.IN_PROGRESS,
                _
        ) >> 4

        1 * flightRepository.landFlights(
                Flights.IN_PROGRESS,
                Flights.LANDED,
                _
        ) >> 2
    }
}