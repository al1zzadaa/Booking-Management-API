package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.AccountRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest;
import com.example.bookingmanagementapi.dto.response.AccountResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")
    AccountResponse toDto(AccountEntity account);

    List<AccountResponse> toListDto(List<AccountEntity> accounts);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userId", target = "user.id")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccount(UpdateAccountRequest updateAccountRequest, @MappingTarget AccountEntity accountEntity);
}