package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.SubscriptionPlanRequest
import com.example.bookingmanagementapi.dto.response.SubscriptionPlanResponse
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.SubscriptionPlanMapper
import com.example.bookingmanagementapi.repository.SubscriptionPlanRepository
import com.example.bookingmanagementapi.service.impl.SubscriptionPlanServiceImpl
import spock.lang.Specification

class SubscriptionPlanServiceTest extends Specification {

    def subscriptionPlanRepository = Mock(SubscriptionPlanRepository)
    def subscriptionPlanMapper = Mock(SubscriptionPlanMapper)

    def service = new SubscriptionPlanServiceImpl(
            subscriptionPlanRepository,
            subscriptionPlanMapper
    )

    def "should add subscription plan"() {
        given:
        def request = new SubscriptionPlanRequest()
        def entity = new SubscriptionPlanEntity()

        when:
        service.addSubscription(request)

        then:
        1 * subscriptionPlanMapper.toEntity(request) >> entity
        1 * subscriptionPlanRepository.save(entity)
    }

    def "should delete subscription plan"() {
        given:
        def entity = new SubscriptionPlanEntity()

        when:
        service.deleteSubscription(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.of(entity)
        1 * subscriptionPlanRepository.delete(entity)
    }

    def "should throw when deleting non-existing subscription plan"() {
        when:
        service.deleteSubscription(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Subscription Plan Not Found"

        0 * subscriptionPlanRepository.delete(_)
    }

    def "should update subscription plan"() {
        given:
        def request = new SubscriptionPlanRequest()
        def entity = new SubscriptionPlanEntity()

        when:
        service.updateSubscription(1L, request)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.of(entity)
        1 * subscriptionPlanMapper.update(request, entity)
        1 * subscriptionPlanRepository.save(entity)
    }

    def "should throw when updating non-existing subscription plan"() {
        given:
        def request = new SubscriptionPlanRequest()

        when:
        service.updateSubscription(1L, request)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Subscription Plan Not Found"

        0 * subscriptionPlanMapper.update(_, _)
        0 * subscriptionPlanRepository.save(_)
    }

    def "should get subscription plan"() {
        given:
        def entity = new SubscriptionPlanEntity()
        def response = new SubscriptionPlanResponse()

        when:
        def result = service.getSubscription(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.of(entity)
        1 * subscriptionPlanMapper.toDto(entity) >> response

        result == response
    }

    def "should throw when getting non-existing subscription plan"() {
        when:
        service.getSubscription(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Subscription Plan Not Found"

        0 * subscriptionPlanMapper.toDto(_)
    }

    def "should get all subscription plans"() {
        given:
        def entities = [
                new SubscriptionPlanEntity(),
                new SubscriptionPlanEntity()
        ]

        def responses = [
                new SubscriptionPlanResponse(),
                new SubscriptionPlanResponse()
        ]

        when:
        def result = service.getSubscriptions()

        then:
        1 * subscriptionPlanRepository.findAll() >> entities
        1 * subscriptionPlanMapper.toListDto(entities) >> responses

        result == responses
    }

    def "should return empty list when there are no subscription plans"() {
        given:
        def entities = []
        def responses = []

        when:
        def result = service.getSubscriptions()

        then:
        1 * subscriptionPlanRepository.findAll() >> entities
        1 * subscriptionPlanMapper.toListDto(entities) >> responses

        result == responses
    }

    def "should activate subscription plan"() {
        given:
        def entity = new SubscriptionPlanEntity()
        entity.active = false

        when:
        service.activate(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.of(entity)
        1 * subscriptionPlanRepository.save(entity)

        entity.active
    }

    def "should throw when activating non-existing subscription plan"() {
        when:
        service.activate(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Subscription Plan Not Found"

        0 * subscriptionPlanRepository.save(_)
    }

    def "should deactivate subscription plan"() {
        given:
        def entity = new SubscriptionPlanEntity()
        entity.active = true

        when:
        service.deactivate(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.of(entity)
        1 * subscriptionPlanRepository.save(entity)

        !entity.active
    }

    def "should throw when deactivating non-existing subscription plan"() {
        when:
        service.deactivate(1L)

        then:
        1 * subscriptionPlanRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Subscription Plan Not Found"

        0 * subscriptionPlanRepository.save(_)
    }
}