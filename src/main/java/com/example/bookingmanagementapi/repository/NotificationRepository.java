package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.NotificationEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<@NonNull NotificationEntity, @NonNull Long> {

    @Query("select f from NotificationEntity f where f.user.id = :userId")
    Page<@NonNull NotificationEntity> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select f from NotificationEntity f where f.read = false and f.user.id = :userId")
    Page<@NonNull NotificationEntity> findUnreadByUserId(@Param("userId") Long userId, Pageable pageable);
}
