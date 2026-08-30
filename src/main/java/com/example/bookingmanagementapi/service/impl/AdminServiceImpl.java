package com.example.bookingmanagementapi.service.impl;


import com.example.bookingmanagementapi.dto.request.PrivilegeRequest;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.Roles;
import com.example.bookingmanagementapi.exception.AdminException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;


    public void makeAdmin(PrivilegeRequest privilegeRequest) throws AdminException {
        UserEntity userEntity = userRepository.findByEmail(privilegeRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("username not found"));

        if (userEntity.getRoles().contains(Roles.ROLE_ADMIN)) {
            throw new AdminException("user already admin");
        }
        userEntity.getRoles().add(Roles.ROLE_ADMIN);

        userRepository.save(userEntity);

        log.info("User: '{}' become admin", privilegeRequest.getEmail());
    }
}
