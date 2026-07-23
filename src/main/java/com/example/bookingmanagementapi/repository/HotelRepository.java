package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.HotelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<HotelEntity, Long>, JpaSpecificationExecutor<HotelEntity> {

//    List<HotelEntity> findByCountry(String country);
//
//    List<HotelEntity> findByCity(String city);
//
//    List<HotelEntity> findByRating(Integer rating);
//
//    List<HotelEntity> findByDistanceToSea(Integer distanceToSea);
//
//    List<HotelEntity> findByCountryAndCity(String country, String city);
//
//    List<HotelEntity> findByCountryAndRating(String country, Integer rating);
//
//    List<HotelEntity> findByCityAndRating(String city, Integer rating);
//
//    List<HotelEntity> findByRatingAndDistanceToSea(Integer rating, Integer distanceToSea);
//
//    List<HotelEntity> findByDistanceToSeaAndCity(Integer distanceToSea, String city);
//
//    List<HotelEntity> findByHotelName(String hotelName);
//
//    List<HotelEntity> findByDistanceToSeaAndCountry(Integer distanceToSea, String country);

}
