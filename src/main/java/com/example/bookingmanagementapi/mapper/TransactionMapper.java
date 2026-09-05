package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.TransactionRequest;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toDto(TransactionEntity entity);

    TransactionEntity toEntity(TransactionRequest request);
}
