package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.enums.BookingStatus;
import lombok.NonNull;
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
public interface BookingRepository extends JpaRepository<@NonNull BookingEntity, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull BookingEntity> {

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
    List<BookingEntity> findAllByBookingStatusAfterAndPaymentDeadlineBefore
            (BookingStatus bookingStatus, LocalDateTime now);

    boolean existsByRoomIdAndBookingStatusInAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomId,
            List<BookingStatus> statuses,
            LocalDateTime checkOut,
            LocalDateTime checkIn
    );

    Page<@NonNull BookingEntity> findAllByUserId(Long userId, Pageable pageable);
}
