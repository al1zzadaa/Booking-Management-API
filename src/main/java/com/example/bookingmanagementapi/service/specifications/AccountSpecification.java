package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.AccountFilter;
import com.example.bookingmanagementapi.entity.AccountEntity;
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
public class AccountSpecification implements Specification<AccountEntity> {

    private AccountFilter accountFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<AccountEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (accountFilter == null) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();

        if (accountFilter.getCurrency() != null && !accountFilter.getCurrency().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("currency"), accountFilter.getCurrency()));
        }

        if (accountFilter.getActive() != null) {
            predicates.add(criteriaBuilder.equal(root.get("active"), accountFilter.getActive()));
        }

        if (accountFilter.getCreatedFrom() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), accountFilter.getCreatedFrom()));
        }

        if (accountFilter.getCreatedTo() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), accountFilter.getCreatedTo()));
        }

        if (accountFilter.getMinBalance() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("balance"), accountFilter.getMinBalance()));
        }

        if (accountFilter.getMaxBalance() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("balance"), accountFilter.getMaxBalance()));
        }

        if (accountFilter.getUserId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("user"), accountFilter.getUserId()));
        }

        return  criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
