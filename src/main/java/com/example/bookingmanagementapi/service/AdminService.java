package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.PrivilegeRequest;

public interface AdminService {

    void makeAdmin(PrivilegeRequest privilegeRequest);
}
