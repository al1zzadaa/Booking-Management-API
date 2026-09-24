package com.example.bookingmanagementapi.service;


import com.example.bookingmanagementapi.dto.request.UpdateUserRequest;
import com.example.bookingmanagementapi.dto.request.UserRequest;
import com.example.bookingmanagementapi.dto.response.UserResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    void registerUser(UserRequest userRequest);

    void deleteUser(Long userId);

    void updateUser(Long userId, UpdateUserRequest updateUserRequest);

    UserResponse findById(Long userId);

    Page<@NonNull UserResponse> findAll(Pageable pageable);

    void blockUser(Long userId);

    void unblockUser(Long userId);

    UserResponse getCurrentUser(Long userId);

    void validateUserCanBook(Long userId);

    void uploadProfilePhoto(String email, MultipartFile file);

    void deleteProfilePhoto(String email);
}
