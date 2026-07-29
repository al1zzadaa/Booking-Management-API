package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> ,
        JpaSpecificationExecutor<BookingEntity> {

//    Page<BookingEntity> findAll(BookingFilter bookingFilter,  Pageable pageable);

//    @Query("""
//    SELECT b
//    FROM BookingEntity b
//    WHERE b.room.id = :roomId
//      AND b.checkIn < :checkIn
//      AND b.checkOut > :checkOut
//""")
//    List<BookingEntity> findRoomAvailability(
//            @Param("roomId") Long roomId,
//            @Param("checkIn") LocalDateTime checkIn,
//            @Param("checkOut") LocalDateTime checkOut
//    );

    boolean existsByRoomIdAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomId,
            LocalDateTime checkOut,
            LocalDateTime checkIn
    );
}
