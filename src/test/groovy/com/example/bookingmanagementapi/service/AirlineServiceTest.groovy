package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.AirlineRequest
import com.example.bookingmanagementapi.dto.request.UpdateAirlineRequest
import com.example.bookingmanagementapi.dto.response.AirlineResponse
import com.example.bookingmanagementapi.entity.AirlineEntity
import com.example.bookingmanagementapi.enums.Flights
import com.example.bookingmanagementapi.exception.FlightScheduleException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.AirlineMapper
import com.example.bookingmanagementapi.repository.AirlineRepository
import com.example.bookingmanagementapi.repository.FlightRepository
import com.example.bookingmanagementapi.service.impl.AirlineServiceImpl
import spock.lang.Specification

class AirlineServiceTest extends Specification {

    def airlineRepository = Mock(AirlineRepository)
    def airlineMapper = Mock(AirlineMapper)
    def flightRepository = Mock(FlightRepository)

    def airlineService = new AirlineServiceImpl(
            airlineRepository,
            airlineMapper,
            flightRepository
    )

    def "should create airline successfully"() {
        given:
        def request = new AirlineRequest(
                name: "Azerbaijan Airlines",
                model: "Boeing 787",
                country: "Azerbaijan"
        )

        def entity = new AirlineEntity()

        airlineRepository.existsByAirlineNameIgnoreCaseAndModelIgnoreCaseAndCountryIgnoreCase(
                request.name,
                request.model,
                request.country
        ) >> false

        when:
        airlineService.create(request)

        then:
        1 * airlineMapper.toEntity(request) >> entity
        1 * airlineRepository.save(entity)
    }

    def "should get airline by id successfully"() {
        given:
        def entity = new AirlineEntity(
                id: 1L,
                airlineName: "AZAL",
                model: "J2",
                country: "Azerbaijan"
        )
        def response = new AirlineResponse(
                1L, "AZAL", "J2", "Azerbaijan"
        )

        airlineRepository.findById(1L) >> Optional.of(entity)

        when:
        def result = airlineService.getById(1L)

        then:
        1 * airlineMapper.toDto(entity) >> response
        result == response
    }

    def "should throw exception when deleting non-existing airline"() {
        given:
        airlineRepository.findById(1L) >> Optional.empty()

        when:
        airlineService.delete(1L)

        then:
        thrown(NotFoundException)
    }

    def "should throw exception when updating non-existing airline"() {
        given:
        def request = new UpdateAirlineRequest("AZAL", "J3", "Azerbaijan")
        airlineRepository.findById(1L) >> Optional.empty()

        when:
        airlineService.update(request, 1L)

        then:
        thrown(NotFoundException)
    }

    def "should throw exception when getting non-existing airline"() {
        given:
        airlineRepository.findById(1L) >> Optional.empty()

        when:
        airlineService.getById(1L)

        then:
        thrown(NotFoundException)
    }

    def "should update airline successfully"() {
        given:
        def request = new UpdateAirlineRequest("AZAL", "J3", "Azerbaijan")

        def entity = new AirlineEntity(
                id: 1L,
                airlineName: "AZAL",
                model: "J2",
                country: "Azerbaijan"
        )

        airlineRepository.findById(1L) >> Optional.of(entity)

        when:
        airlineService.update(request, 1L)

        then:
        1 * airlineMapper.update(request, entity)
        1 * airlineRepository.save(entity)
    }

    def "should delete airline successfully"() {
        given:
        def airline = new AirlineEntity(
                id: 1L,
                airlineName: "AZAL",
                model: "J2",
                country: "Azerbaijan",
                isActive: true
        )

        airlineRepository.findById(1L) >> Optional.of(airline)

        flightRepository.findAllByAirlineAndStatus(
                airline,
                Flights.SCHEDULED
        ) >> false

        when:
        airlineService.delete(1L)

        then:
        airline.isActive == false
    }

    def "should throw exception when airline has scheduled flight"() {
        given:
        def airline = new AirlineEntity(id: 1L, isActive: true)

        airlineRepository.findById(1L) >> Optional.of(airline)

        flightRepository.findAllByAirlineAndStatus(
                airline,
                Flights.SCHEDULED
        ) >> true

        when:
        airlineService.delete(1L)

        then:
        thrown(FlightScheduleException)

        airline.isActive
    }

}
