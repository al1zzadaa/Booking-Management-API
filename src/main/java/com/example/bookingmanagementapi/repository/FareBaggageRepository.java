package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AirlineEntity;
import com.example.bookingmanagementapi.entity.FareBaggageEntity;
import com.example.bookingmanagementapi.enums.TicketClass;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareBaggageRepository extends JpaRepository<@NonNull FareBaggageEntity, @NonNull Long> {

    @Query("""
       select f from FareBaggageEntity f
       where f.airline = :airline
       and f.ticketClass = :ticketClass
       """)
    Optional<FareBaggageEntity> findByAirlineAndTicketClass(
            @Param("airline") AirlineEntity airline,
            @Param("ticketClass") TicketClass ticketClass);
}
