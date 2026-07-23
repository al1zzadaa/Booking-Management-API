package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.enums.Roles;
import com.example.bookingmanagementapi.enums.UserStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserStatus isActive;
    private Roles role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
