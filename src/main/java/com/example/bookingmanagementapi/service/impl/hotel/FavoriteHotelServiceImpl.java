package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.request.FavoriteHotelRequest;
import com.example.bookingmanagementapi.dto.response.FavoriteHotelResponse;
import com.example.bookingmanagementapi.entity.FavoriteHotelEntity;
import com.example.bookingmanagementapi.mapper.FavoriteHotelMapper;
import com.example.bookingmanagementapi.repository.FavoriteHotelRepository;
import com.example.bookingmanagementapi.service.FavoriteHotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteHotelServiceImpl implements FavoriteHotelService {

    private final FavoriteHotelMapper favoriteHotelMapper;
    private final FavoriteHotelRepository  favoriteHotelRepository;
    @Override
    public void addFavoriteHotel(FavoriteHotelRequest favoriteHotelRequest) {
        FavoriteHotelEntity favoriteHotelEntity = favoriteHotelMapper.toEntity(favoriteHotelRequest);

        favoriteHotelRepository.save(favoriteHotelEntity);
    }

    @Override
    public void removeFavoriteHotel(Long id) {
        favoriteHotelRepository.deleteById(id);
    }

    @Override
    public List<FavoriteHotelResponse> getAll(Long userId) {
        List<FavoriteHotelEntity> favoriteHotelEntities = favoriteHotelRepository.findAllByUserId(userId);

        return favoriteHotelMapper.toListDto(favoriteHotelEntities);
    }
}
