package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.service.specifications.PromoCodeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCodeEntity, Long>,
                                            JpaSpecificationExecutor<PromoCodeEntity> {
    Optional<PromoCodeEntity> findByCode(String code);
}
