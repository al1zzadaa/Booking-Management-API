package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.UpdateUserRequest;
import com.example.bookingmanagementapi.dto.response.UserResponse;
import com.example.bookingmanagementapi.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring" , uses = AccountMapper.class)
public interface UserMapper {

    UserResponse toDto(UserEntity userEntity);

    void updateUser(UserEntity userEntity, @MappingTarget UpdateUserRequest updateUserRequest);

}
