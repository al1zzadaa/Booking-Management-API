package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.SubscriptionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity,Long> {
}
