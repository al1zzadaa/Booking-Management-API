package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.FlightReviewRequest
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest
import com.example.bookingmanagementapi.entity.FlightReviewEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.exception.AccessDeniedException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.FlightReviewMapper
import com.example.bookingmanagementapi.repository.FlightReviewRepository
import com.example.bookingmanagementapi.service.impl.FlightReviewServiceImpl
import com.example.bookingmanagementapi.util.ValidationUtil
import spock.lang.Specification

class FlightReviewServiceTest extends Specification {

    def flightReviewRepository = Mock(FlightReviewRepository)
    def flightReviewMapper = Mock(FlightReviewMapper)
    def validationUtil = Mock(ValidationUtil)

    def flightReviewService = new FlightReviewServiceImpl(
            flightReviewRepository,
            flightReviewMapper,
            validationUtil
    )


    def "createFlightReview should validate, map and save review"() {
        given:
        Long userId = 1L

        def request = new FlightReviewRequest(10L, "interesting!", 4)
        def entity = new FlightReviewEntity()

        when:
        flightReviewService.createFlightReview(userId, request)

        then:
        1 * validationUtil.validateRating(request.getRating())
        1 * flightReviewMapper.toEntity(request) >> entity
        1 * flightReviewRepository.save(entity)
    }

    def "updateFlightReview should update and save review when user owns it"() {
        given:
        Long id = 1L
        Long userId = 10L

        def request = new UpdateFlightReviewRequest()
        def review = new FlightReviewEntity()
        def user = new UserEntity()

        user.setId(userId)
        review.setUser(user)

        when:
        flightReviewService.updateFlightReview(id, userId, request)

        then:
        1 * flightReviewRepository.findById(id) >> Optional.of(review)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(userId, userId)
        1 * flightReviewMapper.updateFlightReview(request, review)
        1 * flightReviewRepository.save(review)
    }

    def "updateFlightReview should throw NotFoundException when review does not exist"() {
        given:
        Long id = 1L
        Long userId = 10L
        def request = new UpdateFlightReviewRequest()

        when:
        flightReviewService.updateFlightReview(id, userId, request)

        then:
        1 * flightReviewRepository.findById(id) >> Optional.empty()
        0 * validationUtil.checkUserIdEqualsToUsedUsedId(_, _)
        0 * flightReviewMapper.updateFlightReview(_, _)
        0 * flightReviewRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "Flight review not found with id: 1"
    }

    def "updateFlightReview should not update review when user is not owner"() {
        given:
        Long id = 1L
        Long userId = 10L
        Long ownerId = 20L

        def request = new UpdateFlightReviewRequest("interesting!", 4)
        def review = new FlightReviewEntity()
        def user = new UserEntity()

        user.setId(ownerId)
        review.setUser(user)

//        validationUtil.checkUserIdEqualsToUsedUsedId(userId, ownerId) >>
//                { throw new AccessDeniedException("Access denied") }

        when:
        flightReviewService.updateFlightReview(id, userId, request)

        then:
        1 * flightReviewRepository.findById(id) >> Optional.of(review)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(userId, ownerId) >>
                { throw new AccessDeniedException("Access denied") }
        0 * flightReviewMapper.updateFlightReview(_, _)
        0* flightReviewRepository.save(_)

        thrown(AccessDeniedException)
    }

    def "deleteFlightReview should delete review when user owns it"() {
        given:
        Long id = 1L
        Long userId = 10L

        def review = new FlightReviewEntity()
        def user = new UserEntity()

        user.setId(userId)
        review.setUser(user)

        when:
        flightReviewService.deleteFlightReview(userId, id)

        then:
        1 * flightReviewRepository.findById(id) >> Optional.of(review)
        1 * validationUtil.checkUserIdEqualsToUsedUsedId(userId, userId)
        1 * flightReviewRepository.deleteById(id)
    }

    def "deleteFlightReview should throw NotFoundException when review does not exist"() {
        given:
        Long id = 1L
        Long userId = 10L

        when:
        flightReviewService.deleteFlightReview(userId, id)

        then:
        1 * flightReviewRepository.findById(id) >> Optional.empty()
        0 * validationUtil.checkUserIdEqualsToUsedUsedId(_, _)

        thrown(NotFoundException)
    }
}