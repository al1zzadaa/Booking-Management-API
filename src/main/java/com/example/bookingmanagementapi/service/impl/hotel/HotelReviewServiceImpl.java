package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.HotelReviewFilter;
import com.example.bookingmanagementapi.dto.request.HotelReviewRequest;
import com.example.bookingmanagementapi.dto.request.UpdateHotelReviewRequest;
import com.example.bookingmanagementapi.dto.response.hotel.HotelReviewResponse;
import com.example.bookingmanagementapi.entity.HotelReviewEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.AccessDeniedException;
import com.example.bookingmanagementapi.mapper.HotelReviewMapper;
import com.example.bookingmanagementapi.repository.HotelRepository;
import com.example.bookingmanagementapi.repository.HotelReviewRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.HotelReviewService;
import com.example.bookingmanagementapi.service.specifications.HotelReviewSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    }

    @Override
    public void updateHotelReview(Long userId, Long id, UpdateHotelReviewRequest updateHotelReviewRequest) {

        HotelReviewEntity hotelReviewEntity = hotelReviewRepository.findById(id)
                .orElseThrow(null);

        validationUtil.checkUserIdEqualsToUsedUsedId(userId, hotelReviewEntity.getUser().getId());

        hotelReviewMapper.updateHotelReview(updateHotelReviewRequest, hotelReviewEntity);
                
        hotelReviewRepository.save(hotelReviewEntity);
    }

    @Override
    public void deleteHotelReview(Long userId, Long id){

        HotelReviewEntity hotelReviewEntity = hotelReviewRepository.findById(id)
                .orElseThrow(null);

        validationUtil.checkUserIdEqualsToUsedUsedId(userId, hotelReviewEntity.getUser().getId());

        hotelReviewRepository.deleteById(id);
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
                .orElseThrow(null);
        return hotelReviewMapper.toDto(hotelReviewEntity);

    }
}
