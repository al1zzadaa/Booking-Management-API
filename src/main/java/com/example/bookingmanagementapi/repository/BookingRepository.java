package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long>,
        JpaSpecificationExecutor<BookingEntity> {

    @Modifying
    @Query("""
                UPDATE BookingEntity b
                SET b.bookingStatus = :newStatus
                WHERE b.bookingStatus = :oldStatus
                  AND b.checkIn <= :now
            """)
    void updateCheckIns(
            @Param("oldStatus") BookingStatus oldStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
                UPDATE BookingEntity b
                SET b.bookingStatus = :newStatus
                WHERE b.bookingStatus = :oldStatus
                  AND b.checkOut <= :now
            """)
    void updateCheckOuts(
            @Param("oldStatus") BookingStatus oldStatus,
            @Param("newStatus") BookingStatus newStatus,
            @Param("now") LocalDateTime now
    );
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

//    boolean existsByRoomIdAndCheckInLessThanAndCheckOutGreaterThan(
//            Long roomId,
//            LocalDateTime checkOut,
//            LocalDateTime checkIn
//    );

    List<BookingEntity> findAllByBookingStatusAfterAndPaymentDeadlineBefore
            (BookingStatus bookingStatus, LocalDateTime now);

    boolean existsByRoomIdAndBookingStatusInAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomId,
            List<BookingStatus> statuses,
            LocalDateTime checkOut,
            LocalDateTime checkIn
    );

    Page<BookingEntity> findAllByUserId(Long userId, Pageable pageable);

    List<BookingEntity> findAllByBookingStatusAndCheckInLessThanEqual(BookingStatus bookingStatus, LocalDateTime now);

    List<BookingEntity> findAllByBookingStatusAndCheckOutLessThanEqual(BookingStatus bookingStatus, LocalDateTime now);

//    @Query("""
//    SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
//    FROM BookingEntity b
//    WHERE b.room.id = :roomId
//    AND (
//        b.checkIn < :checkOut
//        OR
//        b.checkOut > :checkIn
//    )
//""")
//    boolean existsOverlappingBooking(
//            @Param("roomId") Long roomId,
//            @Param("checkIn") LocalDateTime checkIn,
//            @Param("checkOut") LocalDateTime checkOut
//    );
}
