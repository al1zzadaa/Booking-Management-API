package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.HotelEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<@NonNull HotelEntity, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull HotelEntity> {

}
