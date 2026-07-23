package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.FlightReviewFilter;
import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class FlightReviewSpecification implements Specification<FlightReviewEntity> {

    private FlightReviewFilter filter;

    @Override
    public @Nullable Predicate toPredicate(Root<FlightReviewEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (filter == null) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getAirlineId() != null) {
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("flight").get("id"), filter.getAirlineId())));
        }

        if (filter.getFromRating() != null) {
            predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), filter.getFromRating())));
        }

        if (filter.getToRating() != null) {
            predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("rating"), filter.getToRating())));
        }

        if (filter.getFlightId() != null) {
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("flightId").get("id"), filter.getFlightId())));
        }

        if (filter.getUserId() != null) {
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("userId").get("id"), filter.getUserId())));
        }

        if (filter.getFromDate() != null) {
            LocalDate localDate = filter.getFromDate();
            predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("fromDate"), localDate)));
        }

        if (filter.getToDate() != null) {
            LocalDate localDate = filter.getToDate();
            predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("toDate"), localDate)));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
