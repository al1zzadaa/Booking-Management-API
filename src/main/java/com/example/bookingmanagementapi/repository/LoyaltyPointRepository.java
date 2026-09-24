package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.LoyaltyType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyPointRepository extends JpaRepository<@NonNull LoyaltyPointEntity, @NonNull Long> {

    List<LoyaltyPointEntity> findAllByUserId(Long userId);

    @Query("""
    select coalesce(sum(l.points), 0)
    from LoyaltyPointEntity l
    where l.user.id = :userId
      and l.type = :type
    """)
    Integer sumByUserAndType(
            @Param("userId") Long userId,
            @Param("type") LoyaltyType type
    );
}
