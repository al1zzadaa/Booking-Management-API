package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.request.PrivilegeRequest;
import com.example.bookingmanagementapi.security.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/makeAdmin")
    public void makeAdmin(PrivilegeRequest privilegeRequest) {
        adminService.makeAdmin(privilegeRequest);
    }
}