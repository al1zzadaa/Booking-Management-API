package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FavoriteHotelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteHotelRepository extends JpaRepository<FavoriteHotelEntity, Long> {

    List<FavoriteHotelEntity> findAllByUserId(Long userId);

    void deleteAllByUserId(Long userId);

    Optional<FavoriteHotelEntity> findByIdAndUserId(Long id, Long userId);
}
