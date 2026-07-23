package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.entity.HotelReviewEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class HotelReviewSpecification implements Specification<HotelReviewEntity> {

    private HotelReviewFilter hotelReviewFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<HotelReviewEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if(hotelReviewFilter == null) return null;

        List<Predicate> predicates = new ArrayList<>();

        if(hotelReviewFilter.getHotelName() != null && !hotelReviewFilter.getHotelName().isEmpty()){
            predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("hotelId").get("hotelName"), "%"+hotelReviewFilter.getHotelName()+"%")));
        }

        if (hotelReviewFilter.getHotelId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("hotelId").get("id"), hotelReviewFilter.getHotelId()));
        }

        if (hotelReviewFilter.getUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("userId").get("id"), hotelReviewFilter.getUserId()));
        }

        if (hotelReviewFilter.getFromRating() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), hotelReviewFilter.getFromRating()));
        }

        if (hotelReviewFilter.getToRating() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rating"), hotelReviewFilter.getToRating()));
        }


        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
