package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.TransactionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toDto(TransactionEntity entity);

}
