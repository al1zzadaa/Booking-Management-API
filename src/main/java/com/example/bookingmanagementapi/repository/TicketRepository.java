package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Long>,
                                            JpaSpecificationExecutor<TicketEntity> {
    List<TicketEntity> findAllByUserIdAndAccountIdAndFlightIdAndStatus(
            Long userId,
            Long accountId,
            Long flightId,
            TicketStatus status
    );}
