package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.FlightFilter;
import com.example.bookingmanagementapi.entity.FlightEntity;
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
public class FlightSpecification implements Specification<FlightEntity> {

    private FlightFilter flightFilter;
    @Override
    public @Nullable Predicate toPredicate(Root<FlightEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

    if (flightFilter ==  null) {
        return null;
    }

    List<Predicate> predicates = new ArrayList<>();

    if (flightFilter.getAirlineId() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("airlineName"), flightFilter.getAirlineId())));
    }

    if (flightFilter.getArrivalCity() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("arrivalCity"),flightFilter.getArrivalCity())));
    }

    if (flightFilter.getDepartureCity() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("departureCity"),flightFilter.getDepartureCity())));
    }

    if (flightFilter.getDepartureCountry() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("departureCountry"),flightFilter.getDepartureCountry())));
    }

    if (flightFilter.getArrivalCountry() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("arrivalCountry"),flightFilter.getArrivalCountry())));
    }

    if (flightFilter.getDepartureTime() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("departureTime"), flightFilter.getDepartureTime())));
    }

    if (flightFilter.getArrivalTime() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("arrivalTime"), flightFilter.getArrivalTime())));
    }

    if (flightFilter.getMinPrice() != null) {
        predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), flightFilter.getMinPrice())));
    }

    if (flightFilter.getMaxPrice() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), flightFilter.getMaxPrice()));
    }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
