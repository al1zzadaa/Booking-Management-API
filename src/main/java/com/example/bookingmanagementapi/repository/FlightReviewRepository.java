package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightReviewRepository extends JpaRepository<@NonNull FlightReviewEntity, Long>,
                                                JpaSpecificationExecutor<FlightReviewEntity> {


//    @Query("select f from FlightEntity f where f.airlineName = :airlineName")
//    List<FlightReview> findByAirlineName(String airlineName);
//
//    @Query("select f from FlightReview f where f.flightId = :flightId")
//    List<FlightReview> findAllByFlightId(Long flightId);
//
//    @Query("select f from FlightReview f where f.userId = :userId")
//    List<FlightReview> findAllFlightReviewsByUserId(Long userId);
//
//
//    List<FlightReview> findAllByRating(Integer rating);
}
