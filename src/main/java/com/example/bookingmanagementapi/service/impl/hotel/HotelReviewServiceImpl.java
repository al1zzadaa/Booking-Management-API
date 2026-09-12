package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;
import com.example.bookingmanagementapi.entity.HotelReviewEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.HotelReviewMapper;
import com.example.bookingmanagementapi.repository.HotelReviewRepository;
import com.example.bookingmanagementapi.service.HotelReviewService;
import com.example.bookingmanagementapi.service.specifications.HotelReviewSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelReviewServiceImpl implements HotelReviewService {

    private final HotelReviewRepository hotelReviewRepository;
    private final HotelReviewMapper hotelReviewMapper;
    private final ValidationUtil validationUtil;

    @Override
    public void createHotelReview(Long userId, HotelReviewRequest hotelReviewRequest) {

        HotelReviewEntity hotelReviewEntity = hotelReviewMapper.toEntity(hotelReviewRequest);
        hotelReviewRepository.save(hotelReviewEntity);

        log.info("Created hotelReviewEntity by userId {} and request {}", userId,  hotelReviewRequest);
    }

    @Override
    public void updateHotelReview(Long userId, Long id, UpdateHotelReviewRequest updateHotelReviewRequest) {

        HotelReviewEntity hotelReviewEntity = hotelReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HotelReviewEntity not found with id: " + id));

        validationUtil.checkUserIdEqualsToUsedUsedId(userId, hotelReviewEntity.getUser().getId());

        hotelReviewMapper.updateHotelReview(updateHotelReviewRequest, hotelReviewEntity);
                
        hotelReviewRepository.save(hotelReviewEntity);

        log.info("Updated hotelReviewEntity by userId {} and request {}", userId, updateHotelReviewRequest);
    }

    @Override
    public void deleteHotelReview(Long userId, Long id){

        HotelReviewEntity hotelReviewEntity = hotelReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HotelReviewEntity not found with id: " + id));

        validationUtil.checkUserIdEqualsToUsedUsedId(userId, hotelReviewEntity.getUser().getId());

        hotelReviewRepository.deleteById(id);

        log.info("Deleted hotelReviewEntity by userId {} and request {}", userId, id);
    }


    @Override
    public List<HotelReviewResponse> findAll(HotelReviewFilter hotelReviewFilter){

        var specifications = new HotelReviewSpecification(hotelReviewFilter);

        List<HotelReviewEntity> hotelReviewEntities = hotelReviewRepository.findAll(specifications);
        return hotelReviewMapper.toListDto(hotelReviewEntities);
    }

    @Override
    public HotelReviewResponse findById(Long id) {

        HotelReviewEntity hotelReviewEntity = hotelReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("HotelReviewEntity not found with id: " + id));

        return hotelReviewMapper.toDto(hotelReviewEntity);

    }
}
