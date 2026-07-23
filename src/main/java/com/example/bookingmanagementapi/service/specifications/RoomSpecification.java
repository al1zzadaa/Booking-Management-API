package com.example.bookingmanagementapi.service.specifications;

import com.example.bookingmanagementapi.dto.filter.RoomFilter;
import com.example.bookingmanagementapi.entity.RoomEntity;
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
public class RoomSpecification implements Specification<RoomEntity> {

    private RoomFilter roomFilter;

    @Override
    public @Nullable Predicate toPredicate(Root<RoomEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        if (roomFilter == null) {
            return null;
        }

        List<Predicate> predicates = new ArrayList<>();

        if(roomFilter.getRoomType() != null) {
            predicates.add(root.get("roomType").in(roomFilter.getRoomType()));
        }

        if (roomFilter.getRoomName() != null) {
            predicates.add(criteriaBuilder.equal(root.get("roomName"), "%" + roomFilter.getRoomName() + "%"));
        }

        if (roomFilter.getMinPrice() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerNight"), roomFilter.getMinPrice()));
        }

        if (roomFilter.getMaxPrice() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pricePerNight"), roomFilter.getMaxPrice()));
        }

        if (roomFilter.getMinCapacity() != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"), roomFilter.getMinCapacity()));
        }

        if (roomFilter.getMaxCapacity() != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("capacity"), roomFilter.getMaxCapacity()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
