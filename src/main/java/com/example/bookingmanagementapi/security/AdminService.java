package com.example.bookingmanagementapi.security;

import com.example.bookingmanagementapi.dto.request.PrivilegeRequest;

public interface AdminService {

    void makeAdmin(PrivilegeRequest privilegeRequest);
}
