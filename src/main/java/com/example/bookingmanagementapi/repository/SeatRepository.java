package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Long>,
                                        JpaSpecificationExecutor<SeatEntity> {
    boolean findBySeat(String seat);

    @Query("select f from SeatEntity f where f.seat = :seat and f.flight.id = :flightId")
    SeatEntity findBySeatAndFlightId(String seat, Long flightId);

//    @Modifying
//    @Query("update SeatEntity f set f.isAvailable = false where f.id = :seatId")
//    void makeSeatUnavailable(@Param("seatId") Long seatId);
}
