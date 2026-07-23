package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.AirlineFilter;
import com.example.bookingmanagementapi.entity.AirlineEntity;
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
public class AirlineSpecification implements Specification<AirlineEntity> {

    private AirlineFilter airlineFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<AirlineEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (airlineFilter == null) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();

        if (airlineFilter.getCountry() != null && !airlineFilter.getCountry().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get(airlineFilter.getCountry()), airlineFilter.getCountry()));
        }

        if (airlineFilter.getName() != null && !airlineFilter.getName().isEmpty()) {
            predicates.add(criteriaBuilder.like(root.get(airlineFilter.getName()), airlineFilter.getName()));
        }

        if (airlineFilter.getModel() != null && !airlineFilter.getModel().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get(airlineFilter.getModel()), airlineFilter.getModel()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
