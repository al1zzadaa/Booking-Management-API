package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Long>,
                                            JpaSpecificationExecutor<TicketEntity> {
}
