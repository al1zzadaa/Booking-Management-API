package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.filter.RoomFilter;
import com.example.bookingmanagementapi.dto.request.RoomRequest;
import com.example.bookingmanagementapi.dto.response.hotel.RoomResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {

    void createRoom(RoomRequest roomRequest);

    void deleteRoom(Long roomId);

    Page<@NonNull RoomResponse> findAll(RoomFilter roomFilter, Pageable pageable);

    RoomResponse findById(Long id);

    RoomResponse findByRoomNo(Integer roomNo);

}
