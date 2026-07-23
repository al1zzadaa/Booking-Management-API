package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.entity.BookingEntity;
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
public class BookingSpecification implements Specification<BookingEntity> {

    private BookingFilter bookingFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<BookingEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {


        if (bookingFilter == null) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();


        if (bookingFilter.getUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("userId").get("id"), bookingFilter.getUserId()));
        }

        if (bookingFilter.getHotelId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("hotelId").get("id"), bookingFilter.getHotelId()));
        }

        if (bookingFilter.getRoomId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("roomId").get("id"), bookingFilter.getRoomId()));
        }

        if (bookingFilter.getCheckInDate() != null) {
            predicates.add(criteriaBuilder.equal(root.get("checkIn"), bookingFilter.getCheckInDate()));
        }

        if (bookingFilter.getCheckOutDate() != null) {
            predicates.add(criteriaBuilder.equal(root.get("checkOut"), bookingFilter.getCheckOutDate()));
        }

        if (bookingFilter.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"), bookingFilter.getMinPrice()));
        }

        if (bookingFilter.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"), bookingFilter.getMaxPrice()));
        }

        if (bookingFilter.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get("bookingStatus"), bookingFilter.getStatus()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
