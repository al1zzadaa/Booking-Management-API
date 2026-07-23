package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AirlineEntity;
import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.enums.Flights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<FlightEntity,Long>, JpaSpecificationExecutor<FlightEntity> {


    Boolean findAllByAirlineAndStatus(AirlineEntity airline, Flights status);

}
