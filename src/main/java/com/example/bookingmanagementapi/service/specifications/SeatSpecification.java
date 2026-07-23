package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.SeatFilter;
import com.example.bookingmanagementapi.entity.SeatEntity;
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
public class SeatSpecification implements Specification<SeatEntity> {

    private SeatFilter seatFilter;
    @Override
    public @Nullable Predicate toPredicate(Root<SeatEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

    if (seatFilter == null) {
        return null;
    }
    List<Predicate> predicates = new ArrayList<>();

    if (seatFilter.getFlightId() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("flightId"), seatFilter.getFlightId())));
    }

    if (seatFilter.getSeatNumber() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("seatNumber"), seatFilter.getSeatNumber())));
    }

    if (seatFilter.getTicketClass() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("ticketClass"), seatFilter.getTicketClass())));
    }

    if (seatFilter.getIsAvailable() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("isAvailable"), seatFilter.getIsAvailable())));
    }

    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

    }
}
