package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.SeatFilter
import com.example.bookingmanagementapi.dto.request.SeatRequest
import com.example.bookingmanagementapi.dto.response.flight.SeatResponse
import com.example.bookingmanagementapi.entity.SeatEntity
import com.example.bookingmanagementapi.enums.Rows
import com.example.bookingmanagementapi.enums.Tickets
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.exception.ValidationException
import com.example.bookingmanagementapi.mapper.SeatMapper
import com.example.bookingmanagementapi.repository.SeatRepository
import com.example.bookingmanagementapi.service.impl.flight.SeatServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import spock.lang.Specification

@Component
@RequiredArgsConstructor
class SeatServiceTest extends Specification {

    def seatRepository = Mock(SeatRepository)
    def seatMapper = Mock(SeatMapper)
    def validationUtil = Mock(ValidationUtil)


    def seatService = new SeatServiceImpl(
            seatRepository,
            seatMapper,
            validationUtil
    )


    def "should create seat successfully"() {
        given:
        def request = new SeatRequest(
                1L,
                "A1",
                Rows.A,
                1,
                Tickets.ECONOMY
        )

        def entity = new SeatEntity()

        when:
        seatService.createSeat(request)

        then:
        1 * validationUtil.validateSeatRow(Rows.A)
        1 * validationUtil.validateSeatNumber(1)
        1 * validationUtil.validateTicketClass(Tickets.ECONOMY)

        1 * seatRepository.findBySeatAndFlightId("A1", 1L) >> null
        1 * seatMapper.toEntity(request) >> entity
        1 * seatRepository.save(entity)
    }

    def "should throw ValidationException when seat already exists"() {
        given:
        def request = new SeatRequest(
                1L,
                "A1",
                Rows.A,
                1,
                Tickets.ECONOMY
        )

        def existingSeat = new SeatEntity()

        when:
        seatService.createSeat(request)

        then:
        1 * validationUtil.validateSeatRow(Rows.A)
        1 * validationUtil.validateSeatNumber(1)
        1 * validationUtil.validateTicketClass(Tickets.ECONOMY)

        1 * seatRepository.findBySeatAndFlightId("A1", 1L) >> existingSeat

        thrown(ValidationException)

        0 * seatMapper.toEntity(_)
        0 * seatRepository.save(_)
    }

    def "should delete seat successfully"() {
        when:
        seatService.deleteSeat(1L)

        then:
        1 * seatRepository.deleteById(1L)
    }

    def "should get seat successfully"() {
        given:
        def seat = new SeatEntity()
        def response = new SeatResponse()

        when:
        def result = seatService.getSeat(1L)

        then:
        1 * seatRepository.findById(1L) >> Optional.of(seat)
        1 * seatMapper.toDto(seat) >> response

        result == response
    }

    def "should throw NotFoundException when seat does not exist"() {
        when:
        seatService.getSeat(1L)

        then:
        1 * seatRepository.findById(1L) >> Optional.empty()

        thrown(NotFoundException)

        0 * seatMapper.toDto(_)
    }

    def "should get seats successfully"() {
        given:
        def filter = new SeatFilter()
        def pageable = PageRequest.of(0, 10)

        def seat1 = new SeatEntity()
        def seat2 = new SeatEntity()

        def response1 = new SeatResponse()
        def response2 = new SeatResponse()

        def page = new PageImpl<SeatEntity>(
                [seat1, seat2],
                pageable,
                2
        )

        when:
        Page<SeatResponse> result = seatService.getSeats(filter, pageable)

        then:
        1 * seatRepository.findAll(_, pageable) >> page

        1 * seatMapper.toDto(seat1) >> response1
        1 * seatMapper.toDto(seat2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }

    def "should return empty page when no seats exist"() {
        given:
        def filter = new SeatFilter()
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<SeatEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = seatService.getSeats(filter, pageable)

        then:
        1 * seatRepository.findAll(_, pageable) >> page

        0 * seatMapper.toDto(_)

        result.empty
        result.totalElements == 0
    }

}
