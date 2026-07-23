package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.HotelFilter;
import com.example.bookingmanagementapi.dto.request.HotelRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse;
import com.example.bookingmanagementapi.entity.HotelEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.HotelMapper;
import com.example.bookingmanagementapi.repository.HotelRepository;
import com.example.bookingmanagementapi.service.HotelService;
import com.example.bookingmanagementapi.service.specifications.HotelSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final ValidationUtil validationUtil;


    @Override
    public void createHotel(HotelRequest hotelRequest) {

        //TODO validate

        HotelEntity hotelEntity = hotelMapper.toEntity(hotelRequest);
        hotelRepository.save(hotelEntity);
    }

    @Override
    public void updateHotel(Long id, UpdateHotelRequest updateHotelRequest) {

        validationUtil.validateId(id);

        HotelEntity hotelEntity = hotelRepository.findById(id)
                .orElseThrow();

        hotelMapper.updateHotel(updateHotelRequest, hotelEntity);

        hotelRepository.save(hotelEntity);
    }

    @Override
    public void deleteHotel(Long hotelId) {

        validationUtil.validateId(hotelId);

        if(!hotelRepository.existsById(hotelId)){
            throw new NotFoundException("Hotel with id " + hotelId + " not found");
        }

        hotelRepository.deleteById(hotelId);
    }

    @Override
    public Page<@NonNull HotelResponse> findAll(HotelFilter hotelFilter, Pageable pageable) {

        var specification = new HotelSpecification(hotelFilter);

        Page<@NonNull HotelEntity> hotelEntities = hotelRepository.findAll(specification,  pageable);
        return hotelEntities.map(hotelMapper::toDto);
    }


    @Override
    public HotelResponse findById(Long id) {

        validationUtil.validateId(id);

        HotelEntity hotelEntity = hotelRepository.findById(id)
                .orElseThrow();

        return hotelMapper.toDto(hotelEntity);
    }
}
