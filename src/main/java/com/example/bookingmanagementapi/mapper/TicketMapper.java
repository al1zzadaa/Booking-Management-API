package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.entity.TicketEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketResponse toDto(TicketEntity ticketEntity);

    List<TicketResponse> toListDto(List<TicketEntity> ticketEntities);

}
