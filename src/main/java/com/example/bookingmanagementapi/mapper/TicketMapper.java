package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.entity.TicketEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketEntity toEntity(TicketRequest ticketRequest);

    TicketResponse toDto(TicketEntity ticketEntity);

    List<TicketResponse> toListDto(List<TicketEntity> ticketEntities);

}
