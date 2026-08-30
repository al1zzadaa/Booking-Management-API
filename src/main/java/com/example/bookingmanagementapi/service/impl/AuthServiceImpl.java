package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.LoginRequest;
import com.example.bookingmanagementapi.dto.request.RefreshRequest;
import com.example.bookingmanagementapi.dto.response.AuthResponse;
import com.example.bookingmanagementapi.entity.RefreshTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.security.CustomUserDetailsService;
import com.example.bookingmanagementapi.security.JwtService;
import com.example.bookingmanagementapi.security.RefreshTokenService;
import com.example.bookingmanagementapi.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;


    public AuthResponse login(LoginRequest loginRequest) {

        try {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        } catch (AuthenticationException ex) {

            log.warn("Failed login attempt for email={}", loginRequest.getEmail());
            throw ex;
        }

        var userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmail());

        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String access = jwtService.generateAccessToken(userDetails);

        String refresh = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.save(user, refresh);

        return new AuthResponse(
                access,
                refresh
        );
    }


    @Transactional
    public AuthResponse refreshToken(RefreshRequest refreshRequest) {

        RefreshTokenEntity storedToken =
                refreshTokenService.findByToken(refreshRequest.getRefreshToken());

        refreshTokenService.validate(storedToken);

        if (!jwtService.isTokenValid(refreshRequest.getRefreshToken(),
                new User(
                        storedToken.getUser().getEmail(),
                        storedToken.getUser().getPassword(),
                        List.of()
                ))) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractEmail(refreshRequest.getRefreshToken());

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = new User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.name()))
                        .toList()
        );

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        refreshTokenService.deleteByToken(refreshRequest.getRefreshToken());
        refreshTokenService.save(user, newRefreshToken);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }


    @Transactional
    public void logout(RefreshRequest refreshRequest) {

        RefreshTokenEntity tokenEntity =
                refreshTokenService.findByToken(refreshRequest.getRefreshToken());

        refreshTokenService.deleteByToken(tokenEntity.getToken());
    }

}
