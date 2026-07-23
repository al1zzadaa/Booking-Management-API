package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    SubscriptionEntity findByAccountId(Long accountId);

//    Page<@NonNull SubscriptionEntity> findAll(Pageable pageable);

    @Query(value = """
            SELECT *
            FROM subscription_entity
            WHERE auto_renew = true
            AND end_date <= CURRENT_DATE + INTERVAL '1 day'
            """, nativeQuery = true)
    Page<SubscriptionEntity> findDueForRenewal(Pageable pageable);

    SubscriptionEntity findBySubscriptionPlanAndIsActive(SubscriptionPlanEntity subscriptionPlan, Boolean isActive);
}
