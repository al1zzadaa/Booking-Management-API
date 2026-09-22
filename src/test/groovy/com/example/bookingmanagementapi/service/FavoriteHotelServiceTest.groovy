package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse
import com.example.bookingmanagementapi.entity.FavoriteHotelEntity
import com.example.bookingmanagementapi.entity.HotelEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.exception.DuplicateEntityException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.FavoriteHotelMapper
import com.example.bookingmanagementapi.repository.FavoriteHotelRepository
import com.example.bookingmanagementapi.repository.HotelRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.FavoriteHotelServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class FavoriteHotelServiceTest extends Specification {
    def favoriteHotelRepository = Mock(FavoriteHotelRepository)
    def favoriteHotelMapper = Mock(FavoriteHotelMapper)
    def userRepository = Mock(UserRepository)
    def hotelRepository = Mock(HotelRepository)


    def favoriteHotelService = new FavoriteHotelServiceImpl(
            favoriteHotelMapper,
            favoriteHotelRepository,
            hotelRepository,
            userRepository
    )

    def "addFavoriteHotel should save favorite hotel"() {
        given:
        Long userId = 1L

        def request = new FavoriteHotelRequest(10L)
        def user = new UserEntity()
        def hotel = new HotelEntity()

        when:
        favoriteHotelService.addFavoriteHotel(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * hotelRepository.findById(request.hotelId) >> Optional.of(hotel)
        1 * favoriteHotelRepository.existsByUserAndHotel(user, hotel) >> false
        1 * favoriteHotelRepository.save({ it.user == user && it.hotel == hotel })
    }

    def "addFavoriteHotel should throw NotFoundException when user does not exist"() {
        given:
        Long userId = 1L
        def request = new FavoriteHotelRequest()

        when:
        favoriteHotelService.addFavoriteHotel(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.empty()
        0 * hotelRepository.findById(_)
        0 * favoriteHotelRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "User not found with id: 1"
    }

    def "addFavoriteHotel should throw NotFoundException when hotel does not exist"() {
        given:
        Long userId = 1L

        def request = new FavoriteHotelRequest(10L)
        def user = new UserEntity()

        when:
        favoriteHotelService.addFavoriteHotel(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * hotelRepository.findById(request.hotelId) >> Optional.empty()
        0 * favoriteHotelRepository.existsByUserAndHotel(_, _)
        0 * favoriteHotelRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "Hotel not found"
    }

    def "addFavoriteHotel should throw DuplicateEntityException when hotel is already favorite"() {
        given:
        Long userId = 1L

        def request = new FavoriteHotelRequest(10L)

        def user = new UserEntity()
        def hotel = new HotelEntity()

        when:
        favoriteHotelService.addFavoriteHotel(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * hotelRepository.findById(request.hotelId) >> Optional.of(hotel)
        1 * favoriteHotelRepository.existsByUserAndHotel(user, hotel) >> true
        0 * favoriteHotelRepository.save(_)

        def exception = thrown(DuplicateEntityException)
        exception.message == "Hotel is already in favorites"
    }

    def "removeFavoriteHotel should delete favorite hotel"() {
        given:
        Long userId = 1L
        Long id = 100L

        def favoriteHotel = new FavoriteHotelEntity()

        when:
        favoriteHotelService.removeFavoriteHotel(userId, id)

        then:
        1 * favoriteHotelRepository.findByIdAndUserId(id, userId) >> Optional.of(favoriteHotel)
        1 * favoriteHotelRepository.delete(favoriteHotel)
    }

    def "removeFavoriteHotel should throw NotFoundException when favorite hotel does not exist"() {
        given:
        Long userId = 1L
        Long id = 100L

        when:
        favoriteHotelService.removeFavoriteHotel(userId, id)

        then:
        1 * favoriteHotelRepository.findByIdAndUserId(id, userId) >> Optional.empty()
        0 * favoriteHotelRepository.delete(_)

        def exception = thrown(NotFoundException)
        exception.message == "Hotel not found in favorites"
    }

    def "getAll should return paginated favorite hotels"() {
        given:
        Long userId = 1L
        def pageable = PageRequest.of(0, 10)

        def favorite1 = new FavoriteHotelEntity()
        def favorite2 = new FavoriteHotelEntity()

        def response1 = new FavoriteHotelResponse()
        def response2 = new FavoriteHotelResponse()

        def entityPage = new PageImpl<>(
                [favorite1, favorite2],
                pageable,
                2
        )

        when:
        def result = favoriteHotelService.getAll(userId, pageable)

        then:
        1 * favoriteHotelRepository.findAllByUserId(userId, pageable) >> entityPage
        1 * favoriteHotelMapper.toResponse(favorite1) >> response1
        1 * favoriteHotelMapper.toResponse(favorite2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }

    def "clearFavoriteHotels should delete all user's favorite hotels"() {
        given:
        Long userId = 1L

        when:
        favoriteHotelService.clearFavoriteHotels(userId)

        then:
        1 * favoriteHotelRepository.deleteAllByUserId(userId)
    }

    def "getById should return favorite hotel response"() {
        given:
        Long id = 100L

        def favoriteHotel = new FavoriteHotelEntity()
        def response = new FavoriteHotelResponse()

        when:
        def result = favoriteHotelService.getById(id)

        then:
        1 * favoriteHotelRepository.findById(id) >> Optional.of(favoriteHotel)
        1 * favoriteHotelMapper.toResponse(favoriteHotel) >> response

        result == response
    }

    def "getById should throw NotFoundException when favorite hotel does not exist"() {
        given:
        Long id = 100L

        when:
        favoriteHotelService.getById(id)

        then:
        1 * favoriteHotelRepository.findById(id) >> Optional.empty()
        0 * favoriteHotelMapper.toResponse(_)

        def exception = thrown(NotFoundException)
        exception.message == "Hotel not found with id: 100"
    }
}
