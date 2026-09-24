package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.RoomFilter
import com.example.bookingmanagementapi.dto.request.RoomRequest
import com.example.bookingmanagementapi.dto.response.hotel.RoomResponse
import com.example.bookingmanagementapi.entity.RoomEntity
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.RoomMapper
import com.example.bookingmanagementapi.repository.RoomRepository
import com.example.bookingmanagementapi.service.impl.RoomServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class RoomServiceTest extends Specification {

    def roomRepository = Mock(RoomRepository)
    def roomMapper = Mock(RoomMapper)

    def roomService = new RoomServiceImpl(
            roomRepository,
            roomMapper
    )


    def "createRoom should create room successfully"() {
        given:
        def request = new RoomRequest()
        def entity = new RoomEntity()

        when:
        roomService.createRoom(request)

        then:
        1 * roomMapper.toEntity(request) >> entity
        1 * roomRepository.save(entity)
    }


    def "deleteRoom should delete room successfully"() {
        given:
        Long roomId = 1L

        when:
        roomService.deleteRoom(roomId)

        then:
        1 * roomRepository.existsById(roomId) >> true
        1 * roomRepository.deleteById(roomId)
    }


    def "deleteRoom should throw NotFoundException when room does not exist"() {
        given:
        Long roomId = 1L

        when:
        roomService.deleteRoom(roomId)

        then:
        1 * roomRepository.existsById(roomId) >> false
        0 * roomRepository.deleteById(_)

        def exception = thrown(NotFoundException)
        exception.message == "Room not found"
    }


    def "findByRoomNo should return room successfully"() {
        given:
        Integer roomNo = 101

        def entity = new RoomEntity()
        def response = new RoomResponse()

        when:
        def result = roomService.findByRoomNo(roomNo)

        then:
        1 * roomRepository.findByRoomNumber(roomNo) >> entity
        1 * roomMapper.toDto(entity) >> response

        result == response
    }


    def "findAll should return rooms successfully"() {
        given:
        def filter = new RoomFilter()
        def pageable = PageRequest.of(0, 10)

        def room1 = new RoomEntity()
        def room2 = new RoomEntity()

        def response1 = new RoomResponse()
        def response2 = new RoomResponse()

        def page = new PageImpl<RoomEntity>(
                [room1, room2],
                pageable,
                2
        )

        when:
        Page<RoomResponse> result = roomService.findAll(filter, pageable)

        then:
        1 * roomRepository.findAll(_, pageable) >> page

        1 * roomMapper.toDto(room1) >> response1
        1 * roomMapper.toDto(room2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }


    def "findAll should return empty page when no rooms exist"() {
        given:
        def filter = new RoomFilter()
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<RoomEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = roomService.findAll(filter, pageable)

        then:
        1 * roomRepository.findAll(_, pageable) >> page

        0 * roomMapper.toDto(_)

        result.empty
        result.totalElements == 0
    }


    def "findById should return room successfully"() {
        given:
        Long roomId = 1L

        def entity = new RoomEntity()
        def response = new RoomResponse()

        when:
        def result = roomService.findById(roomId)

        then:
        1 * roomRepository.findById(roomId) >> Optional.of(entity)
        1 * roomMapper.toDto(entity) >> response

        result == response
    }


    def "findById should throw NotFoundException when room does not exist"() {
        given:
        Long roomId = 1L

        when:
        roomService.findById(roomId)

        then:
        1 * roomRepository.findById(roomId) >> Optional.empty()
        0 * roomMapper.toDto(_)

        def exception = thrown(NotFoundException)
        exception.message == "Room not found"
    }
}