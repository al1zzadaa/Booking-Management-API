package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FlightBookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBookingEntity,Long> {
}
