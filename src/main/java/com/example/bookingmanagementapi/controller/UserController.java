package com.example.bookingmanagementapi.controller;


import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.AuthResponse;
import com.example.bookingmanagementapi.dto.response.UserResponse;
import com.example.bookingmanagementapi.security.AuthService;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;


    @PostMapping("/register")
    public void register(@RequestBody UserRequest userRequest){
        userService.registerUser(userRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshRequest request) {
        authService.logout(request);
    }

    @GetMapping
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

//    @PutMapping("/{id}")
//    public void updateUser(@PathVariable Long id, @RequestBody UserResponse user){
//    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
    }


    @GetMapping("/me")
    public UserResponse me(
            @AuthenticationPrincipal CustomUserDetails user) {

        return userService.getCurrentUser(user.getId());
    }


}
