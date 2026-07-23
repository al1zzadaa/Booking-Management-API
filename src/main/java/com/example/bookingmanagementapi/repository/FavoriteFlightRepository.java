package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FavoriteFlightEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteFlightRepository extends JpaRepository<@NonNull FavoriteFlightEntity, @NonNull Long> {

    Page<@NonNull FavoriteFlightEntity> findAllByUserId(Long userId, Pageable pageable);

    void deleteAllByUserId(Long userId);
}
