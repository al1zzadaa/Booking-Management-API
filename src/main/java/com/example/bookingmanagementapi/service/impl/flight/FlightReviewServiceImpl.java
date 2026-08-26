package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.FlightReviewFilter;
import com.example.bookingmanagementapi.dto.request.FlightReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFlightReviewRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightReviewResponse;
import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.entity.FlightReviewEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.AccessDeniedException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FlightReviewMapper;
import com.example.bookingmanagementapi.repository.FlightRepository;
import com.example.bookingmanagementapi.repository.FlightReviewRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.FlightReviewService;
import com.example.bookingmanagementapi.service.specifications.FlightReviewSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FlightReviewServiceImpl implements FlightReviewService {

    private final FlightReviewRepository flightReviewRepository;
    private final FlightReviewMapper flightReviewMapper;
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;


    @Transactional
    @Override
    public void createFlightReview(Long userId, FlightReviewRequest flightReviewRequest) {

        validationUtil.validateId(flightReviewRequest.getFlightId());

        validationUtil.validateRating(flightReviewRequest.getRating());
//
//
//        FlightEntity flightEntity = flightRepository.findById(flightReviewRequest.getFlightId()).orElseThrow();
//
//        UserEntity user = userRepository.findById(userId).orElseThrow();
//
//        if (user.getId().equals(flightEntity.))

        FlightReviewEntity flightReview = flightReviewMapper.toEntity(flightReviewRequest);
        flightReviewRepository.save(flightReview);
    }

    @Transactional
    @Override
    public void updateFlightReview(Long id, Long userId, UpdateFlightReviewRequest updateFlightReviewRequest) {

        validationUtil.validateId(id);
        UserEntity user = userRepository.findById(userId).orElseThrow();

        FlightReviewEntity flightReview = flightReviewRepository.findById(id).orElseThrow();

        if (!flightReview.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot update this review");
        }

        flightReviewMapper.updateFlightReview(updateFlightReviewRequest, flightReview);

        flightReviewRepository.save(flightReview);
    }

    @Transactional
    @Override
    public void deleteFlightReview(Long id, Long userId) {

        validationUtil.validateId(id);

        UserEntity user = userRepository.findById(userId).orElseThrow();
        FlightReviewEntity flightReview = flightReviewRepository.findById(id).orElseThrow();

        if (!flightReview.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You cannot delete this review");
        }

        flightReviewRepository.deleteById(id);
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

        validationUtil.validateId(id);

        FlightReviewEntity flightReview = flightReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight Review Not Found"));

        return flightReviewMapper.toDto(flightReview);
    }

}
