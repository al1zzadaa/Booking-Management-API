package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.FareBaggageRequest
import com.example.bookingmanagementapi.dto.response.FareBaggageResponse
import com.example.bookingmanagementapi.entity.FareBaggageEntity
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.FareBaggageMapper
import com.example.bookingmanagementapi.repository.FareBaggageRepository
import com.example.bookingmanagementapi.service.impl.FareBaggageServiceImpl
import spock.lang.Specification

class FareBaggageServiceTest extends Specification {

    def fareBaggageRepository = Mock(FareBaggageRepository)
    def fareBaggageMapper = Mock(FareBaggageMapper)

    def fareBaggageService = new FareBaggageServiceImpl(
            fareBaggageRepository,
            fareBaggageMapper
    )


    def "createFareBaggage should map request and save entity"() {
        given:
        def request = new FareBaggageRequest()
        def entity = new FareBaggageEntity()

        when:
        fareBaggageService.createFareBaggage(request)

        then:
        1 * fareBaggageMapper.toEntity(request) >> entity
        1 * fareBaggageRepository.save(entity)
    }

    def "deleteFareBaggageById should delete baggage when it exists"() {
        given:
        Long id = 1L

        when:
        fareBaggageService.deleteFareBaggageById(id)

        then:
        1 * fareBaggageRepository.existsById(id) >> true
        1 * fareBaggageRepository.deleteById(id)
    }

    def "deleteFareBaggageById should throw NotFoundException when baggage does not exist"() {
        given:
        Long id = 1L

        when:
        fareBaggageService.deleteFareBaggageById(id)

        then:
        1 * fareBaggageRepository.existsById(id) >> false
        0 * fareBaggageRepository.deleteById(_)

        def exception = thrown(NotFoundException)
        exception.message == "FareBaggage not found"
    }

    def "findById should return baggage response when baggage exists"() {
        given:
        Long id = 1L
        def entity = new FareBaggageEntity()
        def response = new FareBaggageResponse()

        when:
        def result = fareBaggageService.findById(id)

        then:
        1 * fareBaggageRepository.findById(id) >> Optional.of(entity)
        1 * fareBaggageMapper.toDto(entity) >> response

        result == response
    }

    def "findById should throw NotFoundException when baggage does not exist"() {
        given:
        Long id = 1L

        when:
        fareBaggageService.findById(id)

        then:
        1 * fareBaggageRepository.findById(id) >> Optional.empty()
        0 * fareBaggageMapper.toDto(_)

        def exception = thrown(NotFoundException)
        exception.message == "FareBaggageEntity not found with id: 1"
    }

    def "getAll should return all baggage responses"() {
        given:
        def entity1 = new FareBaggageEntity()
        def entity2 = new FareBaggageEntity()

        def response1 = new FareBaggageResponse()
        def response2 = new FareBaggageResponse()

        def entities = [entity1, entity2]
        def responses = [response1, response2]

        when:
        def result = fareBaggageService.getAll()

        then:
        1 * fareBaggageRepository.findAll() >> entities
        1 * fareBaggageMapper.toListDto(entities) >> responses

        result == responses
    }

    def "getAll should return empty list when there are no baggage records"() {
        given:
        def entities = []

        when:
        def result = fareBaggageService.getAll()

        then:
        1 * fareBaggageRepository.findAll() >> entities
        1 * fareBaggageMapper.toListDto(entities) >> []

        result == []
    }
}
