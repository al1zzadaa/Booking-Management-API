package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.PrivilegeRequest;
import com.example.bookingmanagementapi.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/makeAdmin")
    public void makeAdmin(@Valid @RequestBody PrivilegeRequest privilegeRequest) {
        adminService.makeAdmin(privilegeRequest);
    }
}