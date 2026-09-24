package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.entity.AccountEntity
import com.example.bookingmanagementapi.entity.SubscriptionEntity
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.repository.SubscriptionRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.SubscriptionAutoRenewServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class SubscriptionAutoRenewServiceTest extends Specification {

    def subscriptionRepository = Mock(SubscriptionRepository)
    def subscriptionService = Mock(SubscriptionService)
    def userRepository = Mock(UserRepository)

    def service = new SubscriptionAutoRenewServiceImpl(
            subscriptionRepository,
            subscriptionService,
            userRepository
    )

    def "should auto renew subscriptions successfully"() {
        given:
        def account = new AccountEntity()
        account.setId(20L)

        def plan = new SubscriptionPlanEntity()
        plan.setId(10L)

        def subscription = new SubscriptionEntity()
        subscription.setId(100L)
        subscription.setAutoRenewAccount(account)
        subscription.setSubscriptionPlan(plan)

        def pageable = PageRequest.of(0, 20)

        def page = new PageImpl<>(
                [subscription],
                pageable,
                1
        )

        def user = new UserEntity()
        user.setId(1L)

        when:
        service.autoRenew()

        then:
        1 * subscriptionRepository.findDueForRenewal(pageable) >> page

        1 * userRepository.findByAccountsId(20L) >> Optional.of(user)

        1 * subscriptionService.renew(
                1L,
                {
                    it.accountId == 20L &&
                            it.planId == 10L
                }
        )

        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])
    }

    def "should fail auto renewal when user is not found"() {
        given:
        def account = new AccountEntity()
        account.id = 20L

        def plan = new SubscriptionPlanEntity()
        plan.id = 10L

        def subscription = new SubscriptionEntity()
        subscription.id = 100L
        subscription.autoRenewAccount = account
        subscription.subscriptionPlan = plan

        def pageable = PageRequest.of(0, 20)

        def page = new PageImpl<>(
                [subscription],
                pageable,
                1
        )

        when:
        service.autoRenew()

        then:
        1 * subscriptionRepository.findDueForRenewal(pageable) >> page

        1 * userRepository.findByAccountsId(20L) >> Optional.empty()

        0 * subscriptionService.renew(_, _)

        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])
    }

    def "should continue when subscription renewal fails"() {
        given:
        def account = new AccountEntity()
        account.id = 20L

        def plan = new SubscriptionPlanEntity()
        plan.id = 10L

        def subscription = new SubscriptionEntity()
        subscription.id = 100L
        subscription.autoRenewAccount = account
        subscription.subscriptionPlan = plan

        def user = new UserEntity()
        user.id = 1L

        def pageable = PageRequest.of(0, 20)

        def page = new PageImpl<>(
                [subscription],
                pageable,
                1
        )

        when:
        service.autoRenew()

        then:
        1 * subscriptionRepository.findDueForRenewal(pageable) >> page

        1 * userRepository.findByAccountsId(20L) >> Optional.of(user)

        1 * subscriptionService.renew(
                1L,
                {
                    it.accountId == 20L &&
                            it.planId == 10L
                }
        ) >> { throw new RuntimeException("Payment failed") }

        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])
    }

    def "should do nothing when there are no subscriptions due for renewal"() {
        given:
        def pageable = PageRequest.of(0, 20)

        when:
        service.autoRenew()

        then:
        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])

        0 * userRepository._
        0 * subscriptionService._
    }

    def "should process multiple subscriptions"() {
        given:
        def account1 = new AccountEntity()
        account1.id = 20L

        def account2 = new AccountEntity()
        account2.id = 30L

        def plan1 = new SubscriptionPlanEntity()
        plan1.id = 10L

        def plan2 = new SubscriptionPlanEntity()
        plan2.id = 40L

        def subscription1 = new SubscriptionEntity()
        subscription1.id = 100L
        subscription1.autoRenewAccount = account1
        subscription1.subscriptionPlan = plan1

        def subscription2 = new SubscriptionEntity()
        subscription2.id = 200L
        subscription2.autoRenewAccount = account2
        subscription2.subscriptionPlan = plan2

        def user1 = new UserEntity()
        user1.id = 1L

        def user2 = new UserEntity()
        user2.id = 2L

        def pageable = PageRequest.of(0, 20)

        def page = new PageImpl<>(
                [subscription1, subscription2],
                pageable,
                2
        )

        when:
        service.autoRenew()

        then:
        1 * subscriptionRepository.findDueForRenewal(pageable) >> page

        1 * userRepository.findByAccountsId(20L) >> Optional.of(user1)
        1 * userRepository.findByAccountsId(30L) >> Optional.of(user2)

        1 * subscriptionService.renew(
                1L,
                {
                    it.accountId == 20L &&
                            it.planId == 10L
                }
        )

        1 * subscriptionService.renew(
                2L,
                {
                    it.accountId == 30L &&
                            it.planId == 40L
                }
        )

        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])
    }

    def "should continue processing other subscriptions when one renewal fails"() {
        given:
        def account1 = new AccountEntity()
        account1.id = 20L

        def account2 = new AccountEntity()
        account2.id = 30L

        def plan1 = new SubscriptionPlanEntity()
        plan1.id = 10L

        def plan2 = new SubscriptionPlanEntity()
        plan2.id = 40L

        def subscription1 = new SubscriptionEntity()
        subscription1.id = 100L
        subscription1.autoRenewAccount = account1
        subscription1.subscriptionPlan = plan1

        def subscription2 = new SubscriptionEntity()
        subscription2.id = 200L
        subscription2.autoRenewAccount = account2
        subscription2.subscriptionPlan = plan2

        def user1 = new UserEntity()
        user1.id = 1L

        def user2 = new UserEntity()
        user2.id = 2L

        def pageable = PageRequest.of(0, 20)

        def page = new PageImpl<>(
                [subscription1, subscription2],
                pageable,
                2
        )

        when:
        service.autoRenew()

        then:
        1 * subscriptionRepository.findDueForRenewal(pageable) >> page

        1 * userRepository.findByAccountsId(20L) >> Optional.of(user1)
        1 * userRepository.findByAccountsId(30L) >> Optional.of(user2)

        1 * subscriptionService.renew(
                1L,
                {
                    it.accountId == 20L &&
                            it.planId == 10L
                }
        ) >> { throw new RuntimeException("Payment failed") }

        1 * subscriptionService.renew(
                2L,
                {
                    it.accountId == 30L &&
                            it.planId == 40L
                }
        )

        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])
    }

    def "should process another page of subscriptions"() {
        given:
        def account1 = new AccountEntity()
        account1.id = 20L

        def plan1 = new SubscriptionPlanEntity()
        plan1.id = 10L

        def subscription1 = new SubscriptionEntity()
        subscription1.id = 100L
        subscription1.autoRenewAccount = account1
        subscription1.subscriptionPlan = plan1

        def user1 = new UserEntity()
        user1.id = 1L

        def pageable = PageRequest.of(0, 20)

        def firstPage = new PageImpl<>(
                [subscription1],
                pageable,
                21
        )

        def account2 = new AccountEntity()
        account2.id = 30L

        def plan2 = new SubscriptionPlanEntity()
        plan2.id = 40L

        def subscription2 = new SubscriptionEntity()
        subscription2.id = 200L
        subscription2.autoRenewAccount = account2
        subscription2.subscriptionPlan = plan2

        def secondPage = new PageImpl<>(
                [subscription2],
                pageable,
                1
        )

        when:
        service.autoRenew()

        then:
        2 * subscriptionRepository.findDueForRenewal(pageable) >>> [
                firstPage,
                secondPage
        ]

        1 * userRepository.findByAccountsId(20L) >> Optional.of(user1)
        1 * userRepository.findByAccountsId(30L) >> Optional.of(user1)

        1 * subscriptionService.renew(
                1L,
                {
                    it.accountId == 20L &&
                            it.planId == 10L
                }
        )

        1 * subscriptionService.renew(
                1L,
                {
                    it.accountId == 30L &&
                            it.planId == 40L
                }
        )

        1 * subscriptionRepository.findDueForRenewal(pageable) >> new PageImpl<>([])
    }
}