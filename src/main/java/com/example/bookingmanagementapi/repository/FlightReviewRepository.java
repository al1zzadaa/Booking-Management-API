package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightReviewRepository extends JpaRepository<@NonNull FlightReviewEntity, @NonNull Long>,
                                                JpaSpecificationExecutor<@NonNull FlightReviewEntity> {
}
