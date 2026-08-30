package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import com.example.bookingmanagementapi.entity.FavoriteFlightEntity;
import com.example.bookingmanagementapi.entity.FavoriteHotelEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FavoriteHotelMapper;
import com.example.bookingmanagementapi.repository.FavoriteHotelRepository;
import com.example.bookingmanagementapi.service.FavoriteHotelService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteHotelServiceImpl implements FavoriteHotelService {

    private final FavoriteHotelMapper favoriteHotelMapper;
    private final FavoriteHotelRepository favoriteHotelRepository;
    private final ValidationUtil validationUtil;

    @Override
    public void addFavoriteHotel(Long userId, FavoriteHotelRequest favoriteHotelRequest) {
        FavoriteHotelEntity favoriteHotelEntity = favoriteHotelMapper.toEntity(favoriteHotelRequest);

        favoriteHotelRepository.save(favoriteHotelEntity);

        log.info("Added to favorite hotels by  userId {}", userId);
    }

    @Override
    public void removeFavoriteHotel(Long userId, Long id) {
        validationUtil.validateId(id);

        FavoriteHotelEntity favoriteHotelEntity = favoriteHotelRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Flight not found in favorites"));

        favoriteHotelRepository.delete(favoriteHotelEntity);

        log.info("Removed from favorite hotels by userId {}", id);
    }

    @Override
    public List<FavoriteHotelResponse> getAll(Long userId) {
        List<FavoriteHotelEntity> favoriteHotelEntities = favoriteHotelRepository.findAllByUserId(userId);

        return favoriteHotelMapper.toListDto(favoriteHotelEntities);
    }

    @Transactional
    @Override
    public void clearFavoriteHotels(Long userId) {

        validationUtil.validateId(userId);

        favoriteHotelRepository.deleteAllByUserId(userId);

    }

    @Transactional(readOnly = true)
    @Override
    public FavoriteHotelResponse getById(Long id) {

        validationUtil.validateId(id);

        FavoriteHotelEntity hotel = favoriteHotelRepository.findById(id)
                .orElseThrow(null);

        return favoriteHotelMapper.toResponse(hotel);
    }
}
