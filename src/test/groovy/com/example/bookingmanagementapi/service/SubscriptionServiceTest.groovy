package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.SubscriptionRequest
import com.example.bookingmanagementapi.dto.response.SubscriptionResponse
import com.example.bookingmanagementapi.entity.AccountEntity
import com.example.bookingmanagementapi.entity.SubscriptionEntity
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.event.*
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.SubscriptionMapper
import com.example.bookingmanagementapi.repository.AccountRepository
import com.example.bookingmanagementapi.repository.SubscriptionPlanRepository
import com.example.bookingmanagementapi.repository.SubscriptionRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.SubscriptionServiceImpl
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

import java.time.LocalDate

class SubscriptionServiceTest extends Specification {

    def subscriptionRepository = Mock(SubscriptionRepository)
    def subscriptionMapper = Mock(SubscriptionMapper)
    def subscriptionPlanRepository = Mock(SubscriptionPlanRepository)
    def accountRepository = Mock(AccountRepository)
    def notificationService = Mock(NotificationService)
    def userRepository = Mock(UserRepository)
    def transactionService = Mock(TransactionService)
    def eventPublisher = Mock(ApplicationEventPublisher)

    def service = new SubscriptionServiceImpl(
            subscriptionRepository,
            subscriptionMapper,
            subscriptionPlanRepository,
            accountRepository,
            notificationService,
            userRepository,
            transactionService,
            eventPublisher
    )

    def "should subscribe user successfully"() {
        given:
        def user = new UserEntity()
        user.id = 1L

        def plan = new SubscriptionPlanEntity()
        plan.id = 10L
        plan.durationDays = 30

        def account = new AccountEntity()
        account.id = 20L

        def request = new SubscriptionRequest()
        request.planId = 10L
        request.accountId = 20L

        when:
        service.subscribe(1L, request)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * subscriptionPlanRepository.findByIdAndActive(10L, true) >> plan
        1 * accountRepository.findById(20L) >> Optional.of(account)

        1 * transactionService.processSubscriptionPayment(
                request,
                10L,
                plan,
                "Payment for subscription"
        )

        1 * subscriptionRepository.save({
            it.user == user &&
                    it.subscriptionPlan == plan &&
                    it.autoRenewAccount == account &&
                    it.autoRenew &&
                    it.isActive &&
                    it.startDate == LocalDate.now() &&
                    it.endDate == LocalDate.now().plusDays(30)
        })

        1 * eventPublisher.publishEvent({
            it instanceof SubscribeEvent &&
                    it.userId() == 1L
        })
    }

    def "should throw when subscribing user does not exist"() {
        given:
        def request = new SubscriptionRequest()
        request.planId = 10L
        request.accountId = 20L

        when:
        service.subscribe(1L, request)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "User Not Found"

        0 * subscriptionPlanRepository._
        0 * accountRepository._
        0 * transactionService._
        0 * subscriptionRepository._
        0 * eventPublisher._
    }

    def "should throw when subscribing account does not exist"() {
        given:
        def user = new UserEntity()
        user.id = 1L

        def plan = new SubscriptionPlanEntity()
        plan.id = 10L
        plan.durationDays = 30

        def request = new SubscriptionRequest()
        request.planId = 10L
        request.accountId = 20L

        when:
        service.subscribe(1L, request)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * subscriptionPlanRepository.findByIdAndActive(10L, true) >> plan
        1 * accountRepository.findById(20L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Account Not Found"

        0 * transactionService._
        0 * subscriptionRepository._
        0 * eventPublisher._
    }

    def "should get current subscription"() {
        given:
        def subscription = new SubscriptionEntity()
        def response = new SubscriptionResponse()

        when:
        def result = service.getCurrentSubscription(1L)

        then:
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription
        1 * subscriptionMapper.toDto(subscription) >> response

        result == response
    }

    def "should get null current subscription when user has no active subscription"() {
        when:
        def result = service.getCurrentSubscription(1L)

        then:
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> null
        1 * subscriptionMapper.toDto(null) >> null

        result == null
    }

    def "should cancel active subscription"() {
        given:
        def plan = new SubscriptionPlanEntity()

        def subscription = new SubscriptionEntity()
        subscription.id = 100L
        subscription.subscriptionPlan = plan
        subscription.isActive = true
        subscription.autoRenew = true
        subscription.endDate = LocalDate.now().plusDays(30)

        when:
        service.cancel(1L)

        then:
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription

        subscription.isActive == false
        subscription.autoRenew == false
        subscription.endDate == LocalDate.now()

        1 * eventPublisher.publishEvent({
            it instanceof SubscriptionCancelledEvent &&
                    it.userId() == 1L
        })

        0 * subscriptionRepository.save(_)
    }

    def "should return true when subscription is active"() {
        when:
        def result = service.isActive(1L)

        then:
        1 * subscriptionRepository.existsByUserIdAndIsActiveTrueAndEndDateAfter(
                1L,
                LocalDate.now()
        ) >> true

        result
    }

    def "should return false when subscription is not active"() {
        when:
        def result = service.isActive(1L)

        then:
        1 * subscriptionRepository.existsByUserIdAndIsActiveTrueAndEndDateAfter(
                1L,
                LocalDate.now()
        ) >> false

        !result
    }

    def "should renew subscription by extending current end date"() {
        given:
        def plan = new SubscriptionPlanEntity()
        plan.id = 20L
        plan.durationDays = 30

        def subscription = new SubscriptionEntity()
        subscription.id = 100L
        subscription.endDate = LocalDate.now().plusDays(5)
        subscription.isActive = true

        def request = new SubscriptionRequest()
        request.planId = 20L
        request.accountId = 50L

        def oldEndDate = subscription.endDate

        when:
        service.renew(1L, request)

        then:
        1 * subscriptionPlanRepository.findById(20L) >> Optional.of(plan)
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription

        1 * transactionService.processSubscriptionPayment(
                request,
                100L,
                plan,
                "Payment for subscription renewal"
        )

        subscription.subscriptionPlan == plan
        subscription.endDate == oldEndDate.plusDays(30)
        subscription.isActive

        1 * eventPublisher.publishEvent({
            it instanceof SubscriptionRenewedEvent &&
                    it.userId() == 1L
        })
    }

    def "should renew subscription when end date is today"() {
        given:
        def plan = new SubscriptionPlanEntity()
        plan.id = 20L
        plan.durationDays = 30

        def subscription = new SubscriptionEntity()
        subscription.id = 100L
        subscription.endDate = LocalDate.now()
        subscription.isActive = true

        def request = new SubscriptionRequest()
        request.planId = 20L
        request.accountId = 50L

        when:
        service.renew(1L, request)

        then:
        1 * subscriptionPlanRepository.findById(20L) >> Optional.of(plan)
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription

        1 * transactionService.processSubscriptionPayment(
                request,
                100L,
                plan,
                "Payment for subscription renewal"
        )

        subscription.endDate == LocalDate.now().plusDays(30)
        subscription.isActive

        1 * eventPublisher.publishEvent({
            it instanceof SubscriptionRenewedEvent &&
                    it.userId() == 1L
        })
    }

    def "should restart expired subscription during renewal"() {
        given:
        def plan = new SubscriptionPlanEntity()
        plan.id = 20L
        plan.durationDays = 30

        def subscription = new SubscriptionEntity()
        subscription.id = 100L
        subscription.startDate = LocalDate.now().minusDays(20)
        subscription.endDate = LocalDate.now().minusDays(5)
        subscription.isActive = false

        def request = new SubscriptionRequest()
        request.planId = 20L
        request.accountId = 50L

        when:
        service.renew(1L, request)

        then:
        1 * subscriptionPlanRepository.findById(20L) >> Optional.of(plan)
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription

        1 * transactionService.processSubscriptionPayment(
                request,
                100L,
                plan,
                "Payment for subscription renewal"
        )

        subscription.subscriptionPlan == plan
        subscription.startDate == LocalDate.now()
        subscription.endDate == LocalDate.now().plusDays(30)
        subscription.isActive

        1 * eventPublisher.publishEvent({
            it instanceof SubscriptionRenewedEvent &&
                    it.userId() == 1L
        })
    }

    def "should throw when renewal plan does not exist"() {
        given:
        def request = new SubscriptionRequest()
        request.planId = 20L
        request.accountId = 50L

        when:
        service.renew(1L, request)

        then:
        1 * subscriptionPlanRepository.findById(20L) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Subscription Plan Not Found"

        0 * subscriptionRepository._
        0 * transactionService._
        0 * eventPublisher._
    }

    def "should enable auto renew"() {
        given:
        def subscription = new SubscriptionEntity()
        subscription.autoRenew = false

        when:
        service.enableAutoRenew(1L)

        then:
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription

        subscription.autoRenew

        1 * eventPublisher.publishEvent({
            it instanceof AutoRenewEnabledEvent &&
                    it.userId() == 1L
        })
    }

    def "should disable auto renew"() {
        given:
        def subscription = new SubscriptionEntity()
        subscription.autoRenew = true

        when:
        service.disableAutoRenew(1L)

        then:
        1 * subscriptionRepository.findByUserIdAndIsActive(1L, true) >> subscription

        !subscription.autoRenew

        1 * eventPublisher.publishEvent({
            it instanceof AutoRenewDisableEvent &&
                    it.userId() == 1L
        })
    }

    def "should deactivate expired subscriptions"() {
        given:
        def subscription1 = new SubscriptionEntity()
        subscription1.isActive = true

        def subscription2 = new SubscriptionEntity()
        subscription2.isActive = true

        def pageable = PageRequest.of(0, 20)

        def page = new PageImpl<>(
                [subscription1, subscription2],
                pageable,
                2
        )

        when:
        service.deactivateExpiredSubscriptions()

        then:
        1 * subscriptionRepository.findExpiredSubscriptions(
                LocalDate.now(),
                pageable
        ) >> page

        1 * subscriptionRepository.findExpiredSubscriptions(
                LocalDate.now(),
                pageable
        ) >> new PageImpl<>([])

        !subscription1.isActive
        !subscription2.isActive
    }

    def "should not deactivate anything when there are no expired subscriptions"() {
        given:
        def pageable = PageRequest.of(0, 20)

        when:
        service.deactivateExpiredSubscriptions()

        then:
        1 * subscriptionRepository.findExpiredSubscriptions(
                LocalDate.now(),
                pageable
        ) >> new PageImpl<>([])
    }
}