package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.FlightFilter
import com.example.bookingmanagementapi.dto.request.FlightRequest
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse
import com.example.bookingmanagementapi.entity.FlightEntity
import com.example.bookingmanagementapi.enums.Flights
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.FlightMapper
import com.example.bookingmanagementapi.repository.FlightRepository
import com.example.bookingmanagementapi.service.impl.flight.FlightServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import spock.lang.Specification

import java.time.LocalDateTime

@Component
@RequiredArgsConstructor
class FlightServiceTest extends Specification {

    def flightRepository = Mock(FlightRepository)
    def flightMapper = Mock(FlightMapper)
    def validationUtil = Mock(ValidationUtil)


    def flightService = new FlightServiceImpl(
            flightRepository,
            flightMapper,
            validationUtil
    )

    def "createFlight should validate time and save flight"() {
        given:
        def request = new FlightRequest(
                "Azerbaijan",
                "Baku",
                "USA",
                "NEW-YORK",
                LocalDateTime.of(2026, 10, 1, 8, 30) ,
                LocalDateTime.of(2026, 10, 1, 10, 35),
                5L,
                13L,
                Flights.SCHEDULED,
                1500.00)

        def entity = new FlightEntity()

        request.setDepartureTime(LocalDateTime.of(2026, 10, 1, 10, 0))
        request.setArrivalTime(LocalDateTime.of(2026, 10, 1, 12, 0))

        when:
        flightService.createFlight(request)

        then:
        1 * validationUtil.checkTime(
                request.getDepartureTime(),
                request.getArrivalTime()
        )

        1 * flightMapper.toEntity(request) >> entity
        1 * flightRepository.save(entity)
    }

    def "createFlight should not save flight when time validation fails"() {
        given:
        def request = new FlightRequest(
                "Azerbaijan",
                "Baku",
                "USA",
                "NEW-YORK",
                LocalDateTime.of(2026, 10, 1, 8, 30) ,
                LocalDateTime.of(2026, 10, 1, 10, 35),
                5L,
                13L,
                Flights.SCHEDULED,
                1500.00)

        request.setDepartureTime(LocalDateTime.of(2026, 10, 1, 12, 0))
        request.setArrivalTime(LocalDateTime.of(2026, 10, 1, 10, 0))

        when:
        flightService.createFlight(request)

        then:
        1 * validationUtil.checkTime(
                request.getDepartureTime(),
                request.getArrivalTime()
        ) >> { throw new IllegalArgumentException("Invalid flight time") }

        0 * flightMapper.toEntity(_)
        0 * flightRepository.save(_)

        thrown(IllegalArgumentException)
    }

    def "deleteFlight should delete flight when it exists"() {
        given:
        Long flightId = 1L

        when:
        flightService.deleteFlight(flightId)

        then:
        1 * flightRepository.existsById(flightId) >> true
        1 * flightRepository.deleteById(flightId)
    }

    def "deleteFlight should throw NotFoundException when flight does not exist"() {
        given:
        Long flightId = 1L

        when:
        flightService.deleteFlight(flightId)

        then:
        1 * flightRepository.existsById(flightId) >> false
        0 * flightRepository.deleteById(_)

        thrown(NotFoundException)
    }

    def "findAll should return paginated flight responses"() {
        given:
        def filter = new FlightFilter()
        def pageable = PageRequest.of(0, 10)

        def flight1 = new FlightEntity()
        def flight2 = new FlightEntity()

        def response1 = new FlightResponse()
        def response2 = new FlightResponse()

        def entityPage = new PageImpl<>(
                [flight1, flight2],
                pageable,
                2
        )

        when:
        def result = flightService.findAll(filter, pageable)

        then:
        1 * flightRepository.findAll(_, pageable) >> entityPage

        1 * flightMapper.toDto(flight1) >> response1
        1 * flightMapper.toDto(flight2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }

    def "findAll should return empty page when no flights exist"() {
        given:
        def filter = new FlightFilter()
        def pageable = PageRequest.of(0, 10)

        def emptyPage = new PageImpl<>(
                [],
                pageable,
                0
        )

        when:
        def result = flightService.findAll(filter, pageable)

        then:
        1 * flightRepository.findAll(_, pageable) >> emptyPage

        0 * flightMapper.toDto(_)

        result.empty
        result.totalElements == 0
    }

    def "findById should return flight response when flight exists"() {
        given:
        Long id = 1L
        def entity = new FlightEntity()
        def response = new FlightResponse()

        when:
        def result = flightService.findById(id)

        then:
        1 * flightRepository.findById(id) >> Optional.of(entity)
        1 * flightMapper.toDto(entity) >> response

        result == response
    }

    def "findById should throw NotFoundException when flight does not exist"() {
        given:
        Long id = 1L

        when:
        flightService.findById(id)

        then:
        1 * flightRepository.findById(id) >> Optional.empty()
        0 * flightMapper.toDto(_)

        thrown(NotFoundException)
    }
}