package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.filter.FlightReviewFilter;
import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FlightReviewMapper;
import com.example.bookingmanagementapi.repository.FlightReviewRepository;
import com.example.bookingmanagementapi.service.FlightReviewService;
import com.example.bookingmanagementapi.service.specifications.FlightReviewSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class FlightReviewServiceImpl implements FlightReviewService {

    private final FlightReviewRepository flightReviewRepository;
    private final FlightReviewMapper flightReviewMapper;
    private final ValidationUtil validationUtil;


    @Transactional
    @Override
    public void createFlightReview(Long userId, FlightReviewRequest flightReviewRequest) {

        FlightReviewEntity flightReview = flightReviewMapper.toEntity(flightReviewRequest);

        flightReviewRepository.save(flightReview);

        log.info("Flight review created successfully by userId {}", userId);
    }

    @Transactional
    @Override
    public void updateFlightReview(Long id, Long userId, UpdateFlightReviewRequest updateFlightReviewRequest) {

        FlightReviewEntity flightReview = flightReviewRepository.findById(id)
                .orElseThrow(() ->  new NotFoundException("Flight review not found with id: " + id));

        validationUtil.checkUserIdEqualsToUsedUsedId(userId, flightReview.getUser().getId());

        flightReviewMapper.updateFlightReview(updateFlightReviewRequest, flightReview);

        flightReviewRepository.save(flightReview);

        log.info("Flight review updated successfully by userId {}", userId);
    }

    @Transactional
    @Override
    public void deleteFlightReview(Long userId, Long id) {

        FlightReviewEntity flightReview = flightReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight review not found with id: " + id));

        validationUtil.checkUserIdEqualsToUsedUsedId(userId, flightReview.getUser().getId());

        flightReviewRepository.deleteById(id);

        log.error("Flight review deleted successfully by userId {}", userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull FlightReviewResponse> findAll(FlightReviewFilter flightReviewFilter, Pageable pageable) {

        var specifications = new FlightReviewSpecification(flightReviewFilter);

        Page<@NonNull FlightReviewEntity> flightReviews = flightReviewRepository.findAll(specifications, pageable);

        return flightReviews.map(flightReviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public FlightReviewResponse findById(Long id) {

        FlightReviewEntity flightReview = flightReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight Review Not Found"));

        return flightReviewMapper.toDto(flightReview);
    }

}
