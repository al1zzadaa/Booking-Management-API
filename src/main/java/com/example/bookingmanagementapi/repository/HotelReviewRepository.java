package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.HotelReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelReviewRepository extends JpaRepository<HotelReviewEntity, Long>,
                                               JpaSpecificationExecutor<HotelReviewEntity> {
}
