package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long>, JpaSpecificationExecutor<RoomEntity> {

//    List<RoomEntity> findAllByRoomCapacity(Integer capacity);
//
//    List<RoomEntity> findAllByPricePerNight(BigDecimal price);
//
//    List<RoomEntity> findAllByRoomCapacityAndPricePerNight(Integer roomCapacity, BigDecimal pricePerNight);
//
//    List<RoomEntity> findAllByRoomType(Rooms roomType);
//
//    List<RoomEntity> findAllByRoomName(String roomName);

    RoomEntity findByRoomNumber(Integer roomNumber);

}
