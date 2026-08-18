package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.UserPromoCodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPromoCodeRepository extends JpaRepository<UserPromoCodeEntity, Long> {

    Page<UserPromoCodeEntity> findAllByUserId(
            Long userId,
            Pageable pageable
    );
}
