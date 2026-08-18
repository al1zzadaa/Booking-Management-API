package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.PromoCodeFilter;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
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

public class PromoCodeSpecification implements Specification<PromoCodeEntity> {

    private PromoCodeFilter promoCodeFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<PromoCodeEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (promoCodeFilter == null) {
            return null;
        }
        List<Predicate> predicates = new ArrayList<>();

        if (promoCodeFilter.getActive() != null) {
            predicates.add(criteriaBuilder.equal(root.get("active"), true));
        }

        if (promoCodeFilter.getCode() != null) {
            predicates.add(criteriaBuilder.equal(root.get("code"), "%" + promoCodeFilter.getCode() + "%" ));
        }

        if (promoCodeFilter.getDiscountType() != null) {
            predicates.add(criteriaBuilder.equal(root.get("discountType"), promoCodeFilter.getDiscountType()));
        }

        if (promoCodeFilter.getMinDiscountValue() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountValue"), promoCodeFilter.getMinDiscountValue()));
        }

        if (promoCodeFilter.getMaxDiscountValue() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("discountValue"), promoCodeFilter.getMaxDiscountValue()));
        }

        if (promoCodeFilter.getStartDate() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), promoCodeFilter.getStartDate()));
        }

        if (promoCodeFilter.getEndDate() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), promoCodeFilter.getEndDate()));
        }

        if (promoCodeFilter.getMinUsageLimit() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("minUsageLimit"), promoCodeFilter.getMinUsageLimit()));
        }

        if (promoCodeFilter.getMaxUsageLimit() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("maxUsageLimit"), promoCodeFilter.getMaxUsageLimit()));
        }

        return  criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
