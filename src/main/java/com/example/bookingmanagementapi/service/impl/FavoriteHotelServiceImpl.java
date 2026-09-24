package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import com.example.bookingmanagementapi.entity.FavoriteHotelEntity;
import com.example.bookingmanagementapi.entity.HotelEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.DuplicateEntityException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FavoriteHotelMapper;
import com.example.bookingmanagementapi.repository.FavoriteHotelRepository;
import com.example.bookingmanagementapi.repository.HotelRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.FavoriteHotelService;
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
public class FavoriteHotelServiceImpl implements FavoriteHotelService {

    private final FavoriteHotelMapper favoriteHotelMapper;
    private final FavoriteHotelRepository favoriteHotelRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    @Override
    public void addFavoriteHotel(Long userId, FavoriteHotelRequest favoriteHotelRequest) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        HotelEntity hotel = hotelRepository.findById(favoriteHotelRequest.getHotelId())
                .orElseThrow(() -> new NotFoundException("Hotel not found"));

        if (favoriteHotelRepository.existsByUserAndHotel(userEntity, hotel)) {
            throw new DuplicateEntityException("Hotel is already in favorites");
        }

        FavoriteHotelEntity favoriteHotel = FavoriteHotelEntity.builder()
                .user(userEntity)
                .hotel(hotel)
                .build();

        favoriteHotelRepository.save(favoriteHotel);

        log.info("Added to favorite hotels by  userId {}", userId);
    }

    @Override
    public void removeFavoriteHotel(Long userId, Long id) {

        FavoriteHotelEntity favoriteHotelEntity = favoriteHotelRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Hotel not found in favorites"));

        favoriteHotelRepository.delete(favoriteHotelEntity);

        log.info("Removed from favorite hotels by userId {}", id);
    }

    @Override
    public Page<@NonNull FavoriteHotelResponse> getAll(Long userId, Pageable pageable) {

        Page<@NonNull FavoriteHotelEntity> favoriteHotelEntities = favoriteHotelRepository
                .findAllByUserId(userId, pageable);

        return favoriteHotelEntities.map(favoriteHotelMapper::toResponse);
    }

    @Transactional
    @Override
    public void clearFavoriteHotels(Long userId) {

        favoriteHotelRepository.deleteAllByUserId(userId);

    }

    @Transactional(readOnly = true)
    @Override
    public FavoriteHotelResponse getById(Long id) {

        FavoriteHotelEntity hotel = favoriteHotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + id));

        return favoriteHotelMapper.toResponse(hotel);
    }
}
