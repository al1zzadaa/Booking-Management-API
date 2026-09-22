package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.HotelFilter;
import com.example.bookingmanagementapi.entity.HotelEntity;
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
public class HotelSpecification implements Specification<HotelEntity> {

    private HotelFilter hotelFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<HotelEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (hotelFilter == null) return null;

        List<Predicate> predicates = new ArrayList<>();

        if (hotelFilter.getCity() != null && !hotelFilter.getCity().isEmpty()) {
            predicates.add(root.get("city").in(hotelFilter.getCity()));
        }

        if (hotelFilter.getCountry() != null && !hotelFilter.getCountry().isEmpty()){
            predicates.add(criteriaBuilder.equal(root.get("country"), hotelFilter.getCountry()));
        }

        if (hotelFilter.getName() != null && !hotelFilter.getName().isEmpty()){
            predicates.add(criteriaBuilder.equal(root.get("hotelName"), "%" + hotelFilter.getName() + "%"));
        }

        if (hotelFilter.getFromRating() != null){
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), hotelFilter.getFromRating()));
        }

        if (hotelFilter.getToRating() != null){
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rating"), hotelFilter.getToRating()));
        }

        if (hotelFilter.getFromDistant() != null){
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("distant"), hotelFilter.getFromDistant()));
        }

        if (hotelFilter.getToDistant() != null){
            predicates.add(criteriaBuilder.equal(root.get("distant"), hotelFilter.getToDistant()));
        }

        return  criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
