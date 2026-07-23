package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query("select f from NotificationEntity f where f.user.id = :userId")
    Page<NotificationEntity> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select f from NotificationEntity f where f.read = false and f.user.id = :userId")
    Page<NotificationEntity> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);
}
