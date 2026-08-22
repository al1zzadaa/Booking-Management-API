package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AirlineEntity;
import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.enums.Flights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity,Long>, JpaSpecificationExecutor<FlightEntity> {


    Boolean findAllByAirlineAndStatus(AirlineEntity airline, Flights status);

    @Modifying
    @Query("""
    UPDATE FlightEntity f
    SET f.status = :newStatus
    WHERE f.status = :oldStatus
      AND f.departureTime <= :now
""")
    int startFlights(
            @Param("oldStatus") Flights oldStatus,
            @Param("newStatus") Flights newStatus,
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
    UPDATE FlightEntity f
    SET f.status = :newStatus
    WHERE f.status = :oldStatus
      AND f.arrivalTime <= :now
""")
    int landFlights(
            @Param("oldStatus") Flights oldStatus,
            @Param("newStatus") Flights newStatus,
            @Param("now") LocalDateTime now
    );
}
