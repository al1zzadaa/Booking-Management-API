package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> ,
        JpaSpecificationExecutor<BookingEntity> {

//    Page<BookingEntity> findAll(BookingFilter bookingFilter,  Pageable pageable);
}
