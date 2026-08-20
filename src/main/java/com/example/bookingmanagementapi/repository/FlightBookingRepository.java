package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FlightBookingEntity;
import com.example.bookingmanagementapi.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBookingEntity,Long> {

    List<FlightBookingEntity> findAllByStatusAndPaymentDeadlineBefore(
             TicketStatus status,
            LocalDateTime deadline
    );
}
