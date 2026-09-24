package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FavoriteHotelEntity;
import com.example.bookingmanagementapi.entity.HotelEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteHotelRepository extends JpaRepository<@NonNull FavoriteHotelEntity, @NonNull Long> {

    Page<@NonNull FavoriteHotelEntity> findAllByUserId(Long userId,  Pageable pageable);

    void deleteAllByUserId(Long userId);

    Optional<FavoriteHotelEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserAndHotel(UserEntity user, HotelEntity hotel);
}
