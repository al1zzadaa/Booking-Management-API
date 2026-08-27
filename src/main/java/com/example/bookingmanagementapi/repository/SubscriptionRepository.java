package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.SubscriptionEntity;
import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    SubscriptionEntity findByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query(value = """
            SELECT *
            FROM subscriptions
            WHERE auto_renew = true
            AND is_active = true
            AND end_date <= CURRENT_DATE
            AND auto_renew_account_id IS NOT NULL
            """, nativeQuery = true)
    Page<@NonNull SubscriptionEntity> findDueForRenewal(Pageable pageable);


    @Query("""
    SELECT s
    FROM SubscriptionEntity s
    WHERE s.isActive = true
      AND s.endDate <= :today
""")
    Page<SubscriptionEntity> findExpiredSubscriptions(
            @Param("today") LocalDate today, Pageable pageable
    );
    boolean existsByUserIdAndIsActiveTrueAndEndDateAfter(Long userId, LocalDate now);
}
