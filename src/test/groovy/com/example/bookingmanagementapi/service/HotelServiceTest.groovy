package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.HotelFilter
import com.example.bookingmanagementapi.dto.request.HotelRequest
import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse
import com.example.bookingmanagementapi.entity.HotelEntity
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.HotelMapper
import com.example.bookingmanagementapi.repository.HotelRepository
import com.example.bookingmanagementapi.service.impl.HotelServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class HotelServiceTest extends Specification {

    def hotelRepository = Mock(HotelRepository)
    def hotelMapper = Mock(HotelMapper)
    def validationUtil = Mock(ValidationUtil)

    def hotelService = new HotelServiceImpl(
            hotelRepository,
            hotelMapper,
            validationUtil
    )


    def "createHotel should create hotel successfully"() {
        given:
        def request = new HotelRequest()
        def entity = new HotelEntity()

        when:
        hotelService.createHotel(request)

        then:
        1 * hotelMapper.toEntity(request) >> entity
        1 * hotelRepository.save(entity)
    }


    def "deleteHotel should delete hotel successfully"() {
        given:
        Long hotelId = 1L

        when:
        hotelService.deleteHotel(hotelId)

        then:
        1 * hotelRepository.existsById(hotelId) >> true
        1 * hotelRepository.deleteById(hotelId)
    }


    def "deleteHotel should throw NotFoundException when hotel does not exist"() {
        given:
        Long hotelId = 1L

        when:
        hotelService.deleteHotel(hotelId)

        then:
        1 * hotelRepository.existsById(hotelId) >> false
        0 * hotelRepository.deleteById(_)

        def exception = thrown(NotFoundException)
        exception.message == "Hotel with id 1 not found"
    }


    def "findById should return hotel successfully"() {
        given:
        Long hotelId = 1L

        def entity = new HotelEntity()
        def response = new HotelResponse()

        when:
        def result = hotelService.findById(hotelId)

        then:
        1 * hotelRepository.findById(hotelId) >> Optional.of(entity)
        1 * hotelMapper.toDto(entity) >> response

        result == response
    }


    def "findById should throw NotFoundException when hotel does not exist"() {
        given:
        Long hotelId = 1L

        when:
        hotelService.findById(hotelId)

        then:
        1 * hotelRepository.findById(hotelId) >> Optional.empty()
        0 * hotelMapper.toDto(_)

        def exception = thrown(NotFoundException)
        exception.message == "Hotel with id 1 not found"
    }


    def "findAll should return hotels successfully"() {
        given:
        def filter = new HotelFilter()
        def pageable = PageRequest.of(0, 10)

        def hotel1 = new HotelEntity()
        def hotel2 = new HotelEntity()

        def response1 = new HotelResponse()
        def response2 = new HotelResponse()

        def page = new PageImpl<HotelEntity>(
                [hotel1, hotel2],
                pageable,
                2
        )

        when:
        Page<HotelResponse> result = hotelService.findAll(filter, pageable)

        then:
        1 * hotelRepository.findAll(_, pageable) >> page

        1 * hotelMapper.toDto(hotel1) >> response1
        1 * hotelMapper.toDto(hotel2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }


    def "findAll should return empty page when no hotels exist"() {
        given:
        def filter = new HotelFilter()
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<HotelEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = hotelService.findAll(filter, pageable)

        then:
        1 * hotelRepository.findAll(_, pageable) >> page

        0 * hotelMapper.toDto(_)

        result.empty
        result.totalElements == 0
    }
}