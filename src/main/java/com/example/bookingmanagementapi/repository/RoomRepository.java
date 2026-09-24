package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.RoomEntity;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<@NonNull RoomEntity, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull RoomEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RoomEntity r WHERE r.id = :id")
    Optional<RoomEntity> findByIdForUpdate(@Param("id") Long id);

    RoomEntity findByRoomNumber(Integer roomNumber);

}
