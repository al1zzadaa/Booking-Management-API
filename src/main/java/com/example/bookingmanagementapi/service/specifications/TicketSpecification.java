package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.entity.TicketEntity;
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
public class TicketSpecification implements Specification<TicketEntity> {

    private TicketFilter ticketFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<TicketEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {


        if (ticketFilter == null) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();

        if (ticketFilter.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), ticketFilter.getMinPrice()));
        }

        if (ticketFilter.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), ticketFilter.getMaxPrice()));
        }

        if (ticketFilter.getFlightId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("flightId"), ticketFilter.getFlightId()));
        }

        if (ticketFilter.getTicketClass() != null) {
            predicates.add(criteriaBuilder.equal(root.get("ticketClass"), ticketFilter.getTicketClass()));
        }

        return  criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
