package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.RoomFilter;
import com.example.bookingmanagementapi.dto.request.RoomRequest;
import com.example.bookingmanagementapi.dto.response.hotel.RoomResponse;
import com.example.bookingmanagementapi.entity.RoomEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.RoomMapper;
import com.example.bookingmanagementapi.repository.RoomRepository;
import com.example.bookingmanagementapi.service.RoomService;
import com.example.bookingmanagementapi.service.specifications.RoomSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final ValidationUtil validationUtil;

    @Override
    public void createRoom(RoomRequest roomRequest) {
        RoomEntity roomEntity = roomMapper.toEntity(roomRequest);
        roomRepository.save(roomEntity);
    }

    @Override
    public void deleteRoom(Long roomId) {
        validationUtil.validateId(roomId);

        if (!roomRepository.existsById(roomId)) {
            throw new NotFoundException("Room not found");
        }

        roomRepository.deleteById(roomId);
    }

    @Override
    public RoomResponse findByRoomNo(Integer roomNo) {

        validationUtil.validateRoomNo(roomNo);

        RoomEntity roomEntity = roomRepository.findByRoomNumber(roomNo);
        return roomMapper.toDto(roomEntity);
    }

    @Override
    public Page<@NonNull RoomResponse> findAll(RoomFilter roomFilter, Pageable pageable) {

        var specification = new RoomSpecification(roomFilter);

        Page<@NonNull RoomEntity> roomEntities = roomRepository.findAll(specification, pageable);
        return roomEntities.map(roomMapper::toDto) ;
    }

    @Override
    public RoomResponse findById(Long id) {

        validationUtil.validateId(id);

        RoomEntity roomEntity = roomRepository.findById(id).orElseThrow(null);
        return roomMapper.toDto(roomEntity);
    }
}
