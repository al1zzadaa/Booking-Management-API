package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.SeatFilter;
import com.example.bookingmanagementapi.dto.request.SeatRequest;
import com.example.bookingmanagementapi.dto.response.flight.SeatResponse;
import com.example.bookingmanagementapi.entity.SeatEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.ValidationException;
import com.example.bookingmanagementapi.mapper.SeatMapper;
import com.example.bookingmanagementapi.repository.SeatRepository;
import com.example.bookingmanagementapi.service.SeatService;
import com.example.bookingmanagementapi.service.specifications.SeatSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;
    private final ValidationUtil validationUtil;

    @Transactional
    @Override
    public void createSeat(SeatRequest seatRequest) {

        validationUtil.validateId(seatRequest.getFlightId());
        validationUtil.validateSeatRow(seatRequest.getSeatRow());
        validationUtil.validateSeatNumber(seatRequest.getSeatNumber());
        validationUtil.validateTicketClass(seatRequest.getTicketClass());


        if (seatRepository.findBySeatAndFlightId(seatRequest.getSeat(), seatRequest.getFlightId()) != null) {
            throw new ValidationException("Seat already exists");
        }

        SeatEntity seat = seatMapper.toEntity(seatRequest);
        seatRepository.save(seat);
    }


    @Transactional
    @Override
    public void deleteSeat(Long id) {

        validationUtil.validateId(id);

        seatRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public SeatResponse getSeat(Long id) {

        validationUtil.validateId(id);

        SeatEntity seatEntity = seatRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Seat not found"));

        return seatMapper.toDto(seatEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull SeatResponse> getSeats(SeatFilter seatFilter, Pageable pageable) {
        var specification = new SeatSpecification(seatFilter);

        Page<@NonNull SeatEntity> seatEntities = seatRepository.findAll(specification, pageable);

        return seatEntities.map(seatMapper::toDto);
    }

//    public void makeSeatUnavailable(Long seatId) {
//        seatRepository.makeSeatUnavailbale(seatId);
//    }
}
