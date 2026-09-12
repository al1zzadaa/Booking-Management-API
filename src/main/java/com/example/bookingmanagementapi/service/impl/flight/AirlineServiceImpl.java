package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.AirlineFilter;
import com.example.bookingmanagementapi.dto.request.AirlineRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAirlineRequest;
import com.example.bookingmanagementapi.dto.response.AirlineResponse;
import com.example.bookingmanagementapi.entity.AirlineEntity;
import com.example.bookingmanagementapi.enums.Flights;
import com.example.bookingmanagementapi.exception.DuplicateEntityException;
import com.example.bookingmanagementapi.exception.FlightScheduleException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.AirlineMapper;
import com.example.bookingmanagementapi.repository.AirlineRepository;
import com.example.bookingmanagementapi.repository.FlightRepository;
import com.example.bookingmanagementapi.service.AirlineService;
import com.example.bookingmanagementapi.service.specifications.AirlineSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository airlineRepository;
    private final AirlineMapper airlineMapper;
    private final FlightRepository flightRepository;

    @Transactional
    @Override
    public void create(AirlineRequest airlineRequest) {

        if(airlineRepository.existsByAirlineNameIgnoreCaseAndModelIgnoreCaseAndCountryIgnoreCase(
                airlineRequest.getName(),
                airlineRequest.getModel(),
                airlineRequest.getCountry())) {
            throw new DuplicateEntityException("Airline already exists");
        }

        AirlineEntity airlineEntity = airlineMapper.toEntity(airlineRequest);

        airlineRepository.save(airlineEntity);
    }

    @Transactional
    @Override
    public void update(UpdateAirlineRequest updateAirlineRequest, Long id) {


        AirlineEntity airlineEntity = airlineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airline not found"));

        airlineMapper.update(updateAirlineRequest, airlineEntity);

        airlineRepository.save(airlineEntity);
    }

    @Transactional
    @Override
    public void delete(Long id) {


        AirlineEntity airlineEntity = airlineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airline not found"));

        if (flightRepository.findAllByAirlineAndStatus(airlineEntity, Flights.SCHEDULED)){
            throw new FlightScheduleException("Airline scheduled for flight");
        }

        airlineEntity.setIsActive(false);
    }

    @Override
    public AirlineResponse getById(Long id) {

        AirlineEntity airlineEntity = airlineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airline not found"));

        return airlineMapper.toDto(airlineEntity);
    }

    @Override
    public Page<@NonNull AirlineResponse> getAll(AirlineFilter airlineFilter, Pageable pageable) {

        var specification = new AirlineSpecification(airlineFilter);

        Page<@NonNull AirlineEntity> airlineEntities = airlineRepository.findAll(specification, pageable);

        return airlineEntities.map(airlineMapper::toDto);
    }

}
