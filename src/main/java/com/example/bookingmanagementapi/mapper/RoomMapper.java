package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.RoomRequest;
import com.example.bookingmanagementapi.dto.request.UpdateRoomRequest;
import com.example.bookingmanagementapi.dto.response.hotel.RoomResponse;
import com.example.bookingmanagementapi.entity.RoomEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponse toDto(RoomEntity roomEntity);

    RoomEntity toEntity(RoomRequest roomRequest);

    List<RoomResponse> toDto(List<RoomEntity> roomEntities);

    void updateRoom(UpdateRoomRequest updateRoomRequest,  @MappingTarget RoomEntity roomEntity);
}
