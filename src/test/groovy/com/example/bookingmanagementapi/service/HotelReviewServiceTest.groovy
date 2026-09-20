package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.HotelReviewRequest
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest
import com.example.bookingmanagementapi.entity.HotelReviewEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.exception.AccessDeniedException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.HotelReviewMapper
import com.example.bookingmanagementapi.repository.HotelReviewRepository
import com.example.bookingmanagementapi.service.impl.hotel.HotelReviewServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import spock.lang.Specification

@Component
@RequiredArgsConstructor
class HotelReviewServiceTest extends Specification {

    def hotelReviewRepository = Mock(HotelReviewRepository)
    def hotelReviewMapper = Mock(HotelReviewMapper)
    def validationUtil = Mock(ValidationUtil)

    def hotelReviewService = new HotelReviewServiceImpl(
            hotelReviewRepository,
            hotelReviewMapper,
            validationUtil
    )

    def "createHotelReview should validate, map and save review"() {
        given:
        Long userId = 1L

        def request = new HotelReviewRequest(10L, "interesting!", 4)
        def entity = new HotelReviewEntity()

        when:
        hotelReviewService.createHotelReview(userId, request)

        then:
        1 * hotelReviewMapper.toEntity(request) >> entity
        1 * hotelReviewRepository.save(entity)
    }

    def "updateHotelReview should update and save review when user owns it"() {
        given:
        Long id = 1L
        Long userId = 10L

        def request = new UpdateHotelReviewRequest("interesting!", 4)
        def review = new HotelReviewEntity()
        def user = new UserEntity()

        user.setId(userId)
        review.setUser(user)

        when:
        hotelReviewService.updateHotelReview(userId, id, request)

        then:
        1 * hotelReviewRepository.findById(id) >> Optional.of(review)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(userId, userId)
        1 * hotelReviewMapper.updateHotelReview(request, review)
        1 * hotelReviewRepository.save(review)
    }

    def "updateHotelReview should throw NotFoundException when review does not exist"() {
        given:
        Long id = 1L
        Long userId = 10L
        def request = new UpdateHotelReviewRequest("interesting!", 4)

        when:
        hotelReviewService.updateHotelReview( userId, id,request)

        then:
        1 * hotelReviewRepository.findById(id) >> Optional.empty()
        0 * validationUtil.checkUserIdEqualsToUsedUsedId(_, _)
        0 * hotelReviewMapper.updateHotelReview(_, _)
        0 * hotelReviewRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "Hotel review not found with id: 1"
    }

    def "updateHotelReview should not update review when user is not owner"() {
        given:
        Long id = 1L
        Long userId = 10L
        Long ownerId = 20L

        def request = new UpdateHotelReviewRequest("interesting!", 4)
        def review = new HotelReviewEntity()
        def user = new UserEntity()

        user.setId(ownerId)
        review.setUser(user)

        when:
        hotelReviewService.updateHotelReview(userId, id, request)

        then:
        1 * hotelReviewRepository.findById(id) >> Optional.of(review)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(userId, ownerId) >>
                { throw new AccessDeniedException("Access denied") }
        0 * hotelReviewMapper.updateHotelReview(_, _)
        0 * hotelReviewRepository.save(_)

        def exception = thrown(AccessDeniedException)
        exception.message == "Access denied"
    }

    def "deleteHotelReview should delete review when user owns it"() {
        given:
        Long id = 1L
        Long userId = 10L

        def review = new HotelReviewEntity()
        def user = new UserEntity()

        user.setId(userId)
        review.setUser(user)

        when:
        hotelReviewService.deleteHotelReview(userId, id)

        then:
        1 * hotelReviewRepository.findById(id) >> Optional.of(review)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(userId, userId)
        1 * hotelReviewRepository.deleteById(id)
    }

    def "deleteHotelReview should throw NotFoundException when review does not exist"() {
        given:
        Long id = 1L
        Long userId = 10L

        when:
        hotelReviewService.deleteHotelReview(userId, id)

        then:
        1 * hotelReviewRepository.findById(id) >> Optional.empty()
        0 * validationUtil.checkUserIdEqualsToUsedUsedId(_, _)

        thrown(NotFoundException)
    }

}
