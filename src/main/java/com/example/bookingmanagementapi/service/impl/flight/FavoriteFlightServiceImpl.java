package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.request.UpdateFavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.entity.FavoriteFlightEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FavoriteFlightMapper;
import com.example.bookingmanagementapi.repository.FavoriteFlightRepository;
import com.example.bookingmanagementapi.service.FavoriteFlightService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteFlightServiceImpl implements FavoriteFlightService {

    private final FavoriteFlightRepository favoriteFlightRepository;
    private final FavoriteFlightMapper favoriteFlightMapper;
    private final ValidationUtil validationUtil;

    @Transactional
    @Override
    public void addFavoriteFlight(FavoriteFlightRequest favoriteFlightRequest) {

        FavoriteFlightEntity favoriteFlightEntity = favoriteFlightMapper.toEntity(favoriteFlightRequest);

        favoriteFlightRepository.save(favoriteFlightEntity);
    }

    @Transactional
    @Override
    public void removeFavoriteFlight(Long id) {

        validationUtil.validateId(id);

        if(!favoriteFlightRepository.existsById(id)){
            throw new NotFoundException("Product not found in favorites");
        }

        favoriteFlightRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull FavoriteFlightResponse> getAll(Long userId, Pageable pageable) {

        validationUtil.validateId(userId);

        Page<@NonNull FavoriteFlightEntity> list = favoriteFlightRepository.findAllByUserId(userId, pageable);

        return list.map(favoriteFlightMapper::toResponse);
    }

    @Transactional
    @Override
    public void clearFavoriteFlights(Long userId) {

        validationUtil.validateId(userId);

        favoriteFlightRepository.deleteAllByUserId(userId);

    }

    @Transactional(readOnly = true)
    @Override
    public FavoriteFlightResponse getById(Long id) {

        validationUtil.validateId(id);

        FavoriteFlightEntity flight = favoriteFlightRepository.findById(id)
                .orElseThrow(null);

        return favoriteFlightMapper.toResponse(flight);
    }

    @Transactional
    @Override
    public void updateFavoriteFlight(Long id, UpdateFavoriteFlightRequest updateFavoriteFlightRequest) {
        validationUtil.validateId(id);

        FavoriteFlightEntity favoriteFlightEntity = favoriteFlightRepository.findById(id)
                .orElseThrow(null);

        favoriteFlightMapper.updateFavoriteFlight(updateFavoriteFlightRequest, favoriteFlightEntity);

        favoriteFlightRepository.save(favoriteFlightEntity);

    }

}
