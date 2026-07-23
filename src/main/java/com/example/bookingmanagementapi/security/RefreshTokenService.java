package com.example.bookingmanagementapi.security;

import com.example.bookingmanagementapi.entity.RefreshTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;

public interface RefreshTokenService {

    RefreshTokenEntity save(UserEntity user, String token);

     RefreshTokenEntity findByToken(String token);

     void validate(RefreshTokenEntity refreshToken);

     void delete(Long id);

     void deleteByToken(String token);

     void deleteAllByUser(Long  userId);
}
