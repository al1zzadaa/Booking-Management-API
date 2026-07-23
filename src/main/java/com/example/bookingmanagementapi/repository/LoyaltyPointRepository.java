package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import com.example.bookingmanagementapi.enums.LoyaltyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPointEntity, Long> {

    List<LoyaltyPointEntity> findAllByUserId(Long userId);

    @Query("select sum(l.points) " +
            "from LoyaltyPointEntity l " +
            "where l.user = :userId and l.type = : loyaytyType")
    Integer sumByUserAndType(Long userId, LoyaltyType loyaltyType);
}
