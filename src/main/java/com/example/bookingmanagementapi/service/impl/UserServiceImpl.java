package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.UpdateUserRequest;
import com.example.bookingmanagementapi.dto.request.UserRequest;
import com.example.bookingmanagementapi.dto.response.UserResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.Roles;
import com.example.bookingmanagementapi.enums.UserStatus;
import com.example.bookingmanagementapi.exception.*;
import com.example.bookingmanagementapi.mapper.UserMapper;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.AccountService;
import com.example.bookingmanagementapi.service.EmailVerificationService;
import com.example.bookingmanagementapi.service.UserService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;
    private final EmailVerificationService emailVerificationService;

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

        log.info("Created account for user {}", userEntity.getEmail());

        emailVerificationService.create(userEntity);
    }


    @Transactional
    @Override
    public void deleteUser(Long userId) {

        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        entity.setIsActive(UserStatus.DELETED);

        log.info("Deleted user {}", entity.getEmail());
    }

    @Transactional
    @Override
    public void updateUser(Long userId, UpdateUserRequest updateUserRequest) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userMapper.updateUser(userEntity, updateUserRequest);

        userRepository.save(userEntity);

        log.info("Updated user {} to update request {}", userEntity.getEmail(), updateUserRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse findById(Long userId) {

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

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userEntity.setIsActive(UserStatus.BLOCKED);

        log.info("Blocked user {}", userEntity.getEmail());
    }

    @Transactional
    @Override
    public void unblockUser(Long userId) {

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userEntity.setIsActive(UserStatus.ACTIVE);

        log.info("Unblocked user {}", userEntity.getEmail());
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDto(user);
    }

    @Override
    public void validateUserCanBook(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getIsActive().equals(UserStatus.DELETED)) {
            throw new UserBlockedException("User is deleted");
        }

        if (user.getIsActive().equals(UserStatus.BLOCKED)) {
            throw new UserDeletedException("This user is blocked");
        }

        if (user.getIsActive().equals(UserStatus.NOT_VERIFIED)) {
            throw new UserNotVerifiedException("This user is not verified");
        }
    }

    @Override
    @Transactional
    public void uploadProfilePhoto(String email, MultipartFile file) {

        if (file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png"))) {
            throw new ValidationException("Only JPEG and PNG images are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ValidationException("File size must not exceed 5 MB");
        }

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path path = Paths.get("uploads/profile-photos/" + fileName);

            Files.createDirectories(path.getParent());

            Files.write(path, file.getBytes());

            userEntity.setProfilePhotoUrl("/uploads/profile-photos/" + fileName);

            log.info("Profile photo uploaded successfully for user {}", email);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile photo");
        }

    }

    @Override
    @Transactional
    public void deleteProfilePhoto(String email) {

        System.out.println("Deleting profile photo for user " + email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProfilePhotoUrl() == null) {
            return;
        }

        try {
            Path path = Paths.get(user.getProfilePhotoUrl().substring(1));

            Files.deleteIfExists(path);

            user.setProfilePhotoUrl(null);

            log.info("Profile photo deleted successfully for user {}", email);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete profile photo");
        }
    }
}
