package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.HotelReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelReviewRepository extends JpaRepository<HotelReviewEntity, Long>,
                                               JpaSpecificationExecutor<HotelReviewEntity> {

//    @Query("select r from HotelEntity h join h.reviews r where h.id = :hotelId")
//    List<HotelReview> findByHotelId(Long hotelId);
//
//    @Query("select r from HotelEntity h join h.reviews r where h.hotelName = :hotelName")
//    List<HotelReview> findByName(String hotelName);
//
//    @Query("select u from HotelReview u where u.userId = :userId")
//    List<HotelReview> findAllHotelReviewsByUserId(Long userId);
//
//    List<HotelReview> findAllByRating(Integer rating);

}
