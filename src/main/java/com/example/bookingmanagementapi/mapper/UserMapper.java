package com.example.bookingmanagementapi.mapper;

import com.example.bookingmanagementapi.dto.request.AuthRequest;
import com.example.bookingmanagementapi.dto.request.UpdateUserRequest;
import com.example.bookingmanagementapi.dto.request.UserRequest;
import com.example.bookingmanagementapi.dto.response.UserResponse;
import com.example.bookingmanagementapi.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toDto(UserEntity userEntity);

    UserEntity toEntity(UserRequest userRequest);

    List<UserResponse> toListDto(List<UserEntity> userEntities);

    void updateUser(UserEntity userEntity, @MappingTarget UpdateUserRequest updateUserRequest);


//    UserResponse toDto(User);

    UserEntity toEntity(AuthRequest authRequest);


}
