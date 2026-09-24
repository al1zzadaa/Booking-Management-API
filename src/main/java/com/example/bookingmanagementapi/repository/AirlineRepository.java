package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.dto.filter.AirlineFilter;
import com.example.bookingmanagementapi.entity.AirlineEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AirlineRepository extends JpaRepository<@NonNull AirlineEntity,@NonNull Long>,
        JpaSpecificationExecutor<@NonNull AirlineEntity> {

    boolean existsByAirlineNameIgnoreCaseAndModelIgnoreCaseAndCountryIgnoreCase(
            String name,
            String model,
            String country
    );
}
