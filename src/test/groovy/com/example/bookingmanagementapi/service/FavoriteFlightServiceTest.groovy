package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse
import com.example.bookingmanagementapi.entity.FavoriteFlightEntity
import com.example.bookingmanagementapi.entity.FlightEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.exception.DuplicateEntityException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.FavoriteFlightMapper
import com.example.bookingmanagementapi.repository.FavoriteFlightRepository
import com.example.bookingmanagementapi.repository.FlightRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.flight.FavoriteFlightServiceImpl
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import spock.lang.Specification

@Component
@RequiredArgsConstructor
class FavoriteFlightServiceTest extends Specification {

    def favoriteFlightRepository = Mock(FavoriteFlightRepository)
    def favoriteFlightMapper = Mock(FavoriteFlightMapper)
    def userRepository = Mock(UserRepository)
    def flightRepository = Mock(FlightRepository)


    def favoriteFlightService = new FavoriteFlightServiceImpl(
            favoriteFlightRepository,
            favoriteFlightMapper,
            userRepository,
            flightRepository
    )

    def "addFavoriteFlight should save favorite flight"() {
        given:
        Long userId = 1L

        def request = new FavoriteFlightRequest(10L)
        def user = new UserEntity()
        def flight = new FlightEntity()

        when:
        favoriteFlightService.addFavoriteFlight(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * flightRepository.findById(request.flightId) >> Optional.of(flight)
        1 * favoriteFlightRepository.existsByUserAndFlight(user, flight) >> false
        1 * favoriteFlightRepository.save({ it.user == user && it.flight == flight })
    }

    def "addFavoriteFlight should throw NotFoundException when user does not exist"() {
        given:
        Long userId = 1L
        def request = new FavoriteFlightRequest()

        when:
        favoriteFlightService.addFavoriteFlight(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.empty()
        0 * flightRepository.findById(_)
        0 * favoriteFlightRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "User not found with id: 1"
    }

    def "addFavoriteFlight should throw NotFoundException when flight does not exist"() {
        given:
        Long userId = 1L

        def request = new FavoriteFlightRequest(10L)
        def user = new UserEntity()

        when:
        favoriteFlightService.addFavoriteFlight(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * flightRepository.findById(request.flightId) >> Optional.empty()
        0 * favoriteFlightRepository.existsByUserAndFlight(_, _)
        0 * favoriteFlightRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "Flight not found"
    }

    def "addFavoriteFlight should throw DuplicateEntityException when flight is already favorite"() {
        given:
        Long userId = 1L

        def request = new FavoriteFlightRequest(10L)

        def user = new UserEntity()
        def flight = new FlightEntity()

        when:
        favoriteFlightService.addFavoriteFlight(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * flightRepository.findById(request.flightId) >> Optional.of(flight)
        1 * favoriteFlightRepository.existsByUserAndFlight(user, flight) >> true
        0 * favoriteFlightRepository.save(_)

        def exception = thrown(DuplicateEntityException)
        exception.message == "Flight is already in favorites"
    }

    def "removeFavoriteFlight should delete favorite flight"() {
        given:
        Long userId = 1L
        Long id = 100L

        def favoriteFlight = new FavoriteFlightEntity()

        when:
        favoriteFlightService.removeFavoriteFlight(userId, id)

        then:
        1 * favoriteFlightRepository.findByIdAndUserId(id, userId) >> Optional.of(favoriteFlight)
        1 * favoriteFlightRepository.delete(favoriteFlight)
    }

    def "removeFavoriteFlight should throw NotFoundException when favorite flight does not exist"() {
        given:
        Long userId = 1L
        Long id = 100L

        when:
        favoriteFlightService.removeFavoriteFlight(userId, id)

        then:
        1 * favoriteFlightRepository.findByIdAndUserId(id, userId) >> Optional.empty()
        0 * favoriteFlightRepository.delete(_)

        def exception = thrown(NotFoundException)
        exception.message == "Flight not found in favorites"
    }

    def "getAll should return paginated favorite flights"() {
        given:
        Long userId = 1L
        def pageable = PageRequest.of(0, 10)

        def favorite1 = new FavoriteFlightEntity()
        def favorite2 = new FavoriteFlightEntity()

        def response1 = new FavoriteFlightResponse()
        def response2 = new FavoriteFlightResponse()

        def entityPage = new PageImpl<>(
                [favorite1, favorite2],
                pageable,
                2
        )

        when:
        def result = favoriteFlightService.getAll(userId, pageable)

        then:
        1 * favoriteFlightRepository.findAllByUserId(userId, pageable) >> entityPage
        1 * favoriteFlightMapper.toResponse(favorite1) >> response1
        1 * favoriteFlightMapper.toResponse(favorite2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }

    def "clearFavoriteFlights should delete all user's favorite flights"() {
        given:
        Long userId = 1L

        when:
        favoriteFlightService.clearFavoriteFlights(userId)

        then:
        1 * favoriteFlightRepository.deleteAllByUserId(userId)
    }

    def "getById should return favorite flight response"() {
        given:
        Long id = 100L

        def favoriteFlight = new FavoriteFlightEntity()
        def response = new FavoriteFlightResponse()

        when:
        def result = favoriteFlightService.getById(id)

        then:
        1 * favoriteFlightRepository.findById(id) >> Optional.of(favoriteFlight)
        1 * favoriteFlightMapper.toResponse(favoriteFlight) >> response

        result == response
    }

    def "getById should throw NotFoundException when favorite flight does not exist"() {
        given:
        Long id = 100L

        when:
        favoriteFlightService.getById(id)

        then:
        1 * favoriteFlightRepository.findById(id) >> Optional.empty()
        0 * favoriteFlightMapper.toResponse(_)

        def exception = thrown(NotFoundException)
        exception.message == "Flight not found with id: 100"
    }
}
