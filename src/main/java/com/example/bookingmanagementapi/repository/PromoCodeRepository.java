package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCodeEntity, Long> {
    PromoCodeEntity findByCode(String code);
}
