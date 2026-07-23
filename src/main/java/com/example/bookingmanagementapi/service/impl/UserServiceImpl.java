package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.ForgotPasswordRequest;
import com.example.bookingmanagementapi.dto.request.ResetPasswordRequest;
import com.example.bookingmanagementapi.dto.request.UpdateUserRequest;
import com.example.bookingmanagementapi.dto.request.UserRequest;
import com.example.bookingmanagementapi.dto.response.UserResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.EmailVerificationTokenEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.Roles;
import com.example.bookingmanagementapi.enums.TokenType;
import com.example.bookingmanagementapi.enums.UserStatus;
import com.example.bookingmanagementapi.exception.InvalidTokenException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.TokenExpiredException;
import com.example.bookingmanagementapi.mapper.UserMapper;
import com.example.bookingmanagementapi.repository.EmailVerificationTokenRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.AccountService;
import com.example.bookingmanagementapi.service.EmailService;
import com.example.bookingmanagementapi.service.EmailVerificationService;
import com.example.bookingmanagementapi.service.UserService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ValidationUtil validationUtil;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Transactional
    @Override
    public void registerUser(UserRequest userRequest) {

        var roles = Roles.ROLE_USER;

        UserEntity userEntity = UserEntity.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .phoneNumber(userRequest.getPhoneNumber())
                .dateOfBirth(userRequest.getDateOfBirth())
                .roles(Set.of(roles))
                .build();


        AccountEntity account =
                accountService.createDefaultAccount(userEntity, userRequest.getCurrency());

        userEntity.getAccounts().add(account);

        userRepository.save(userEntity);

        emailVerificationService.create(userEntity);
    }


    @Transactional
    @Override
    public void deleteUser(Long userId) {

        validationUtil.validateId(userId);

        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        entity.setIsActive(UserStatus.DELETED);
    }

    @Transactional
    @Override
    public void updateUser(Long userId, UpdateUserRequest updateUserRequest) {

        validationUtil.validateId(userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userMapper.updateUser(userEntity, updateUserRequest);

        userRepository.save(userEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse findById(Long userId) {

        validationUtil.validateId(userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userMapper.toDto(userEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull UserResponse> findAll(Pageable pageable) {

        Page<@NonNull UserEntity> users = userRepository.findAll(pageable);

        return users.map(userMapper::toDto);
    }

    @Transactional
    @Override
    public void blockUser(Long userId) {

        validationUtil.validateId(userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userEntity.setIsActive(UserStatus.BLOCKED);
    }

    @Transactional
    @Override
    public void unblockUser(Long userId) {

        validationUtil.validateId(userId);

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userEntity.setIsActive(UserStatus.ACTIVE);
    }

    @Override public UserResponse getCurrentUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDto(user);
    }
}
