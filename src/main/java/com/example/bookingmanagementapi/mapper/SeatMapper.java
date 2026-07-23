package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.SeatRequest;
import com.example.bookingmanagementapi.dto.request.UpdateSeatRequest;
import com.example.bookingmanagementapi.dto.response.flight.SeatResponse;
import com.example.bookingmanagementapi.entity.SeatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    SeatResponse toDto(SeatEntity seatEntity);

    SeatEntity toEntity(SeatRequest seatRequest);

    List<SeatResponse> toDto(List<SeatEntity> seatEntityList);

    void updateSeat(UpdateSeatRequest updateSeatRequest, @MappingTarget SeatEntity seatEntity);
}
