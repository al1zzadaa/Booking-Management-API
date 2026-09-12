package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.response.UserResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails user) {
        return userService.getCurrentUser(user.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping(
            value = "/profile-photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void uploadProfilePhoto(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.uploadProfilePhoto(
                userDetails.getEmail(),
                file
        );
    }

    @DeleteMapping("/profile-photo")
    public void deleteProfilePhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.deleteProfilePhoto(userDetails.getEmail());
    }
}
