package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.FlightFilter;
import com.example.bookingmanagementapi.dto.request.FlightRequest;
import com.example.bookingmanagementapi.dto.response.flight.FlightResponse;
import com.example.bookingmanagementapi.entity.FlightEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FlightMapper;
import com.example.bookingmanagementapi.repository.FlightRepository;
import com.example.bookingmanagementapi.service.FlightService;
import com.example.bookingmanagementapi.service.specifications.FlightSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final ValidationUtil validationUtil;

    @Transactional
    @Override
    public void createFlight(FlightRequest flightRequest) {

        validationUtil.checkTime(flightRequest.getDepartureTime(), flightRequest.getArrivalTime());

        flightRepository.save(flightMapper.toEntity(flightRequest));
    }

    @Transactional
    @Override
    public void deleteFlight(Long flightId) {

        if (!flightRepository.existsById(flightId)) {
            throw new NotFoundException("flight not found");
        }

        flightRepository.deleteById(flightId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull FlightResponse> findAll(FlightFilter flightFilter, Pageable pageable) {

        var specification = new FlightSpecification(flightFilter);

        Page<@NonNull FlightEntity> flightEntities = flightRepository.findAll(specification, pageable);

        return flightEntities.map(flightMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public FlightResponse findById(Long id) {

        FlightEntity flightEntity = flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("flight not found"));

        return flightMapper.toDto(flightEntity);
    }

}
