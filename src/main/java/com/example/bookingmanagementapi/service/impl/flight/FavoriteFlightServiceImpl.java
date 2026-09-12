package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.request.FavoriteFlightRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteFlightResponse;
import com.example.bookingmanagementapi.entity.FavoriteFlightEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FavoriteFlightMapper;
import com.example.bookingmanagementapi.repository.FavoriteFlightRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.FavoriteFlightService;
import com.example.bookingmanagementapi.util.ValidationUtil;
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
public class FavoriteFlightServiceImpl implements FavoriteFlightService {

    private final FavoriteFlightRepository favoriteFlightRepository;
    private final FavoriteFlightMapper favoriteFlightMapper;
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void addFavoriteFlight(Long userId, FavoriteFlightRequest favoriteFlightRequest) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        FavoriteFlightEntity favoriteFlightEntity = favoriteFlightMapper.toEntity(favoriteFlightRequest);

        favoriteFlightEntity.setUser(userEntity);

        favoriteFlightRepository.save(favoriteFlightEntity);

        log.info("Added to favorite flights by  userId {}", userId);
    }

    @Transactional
    @Override
    public void removeFavoriteFlight(Long userId, Long id) {

        FavoriteFlightEntity favoriteFlight = favoriteFlightRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Flight not found in favorites"));

        favoriteFlightRepository.delete(favoriteFlight);

        log.info("Removed from favorite flights by   userId {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull FavoriteFlightResponse> getAll(Long userId, Pageable pageable) {

        Page<@NonNull FavoriteFlightEntity> list = favoriteFlightRepository.findAllByUserId(userId, pageable);

        return list.map(favoriteFlightMapper::toResponse);
    }

    @Transactional
    @Override
    public void clearFavoriteFlights(Long userId) {

        favoriteFlightRepository.deleteAllByUserId(userId);

    }

    @Transactional(readOnly = true)
    @Override
    public FavoriteFlightResponse getById(Long id) {

        FavoriteFlightEntity flight = favoriteFlightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + id));

        return favoriteFlightMapper.toResponse(flight);
    }
}
