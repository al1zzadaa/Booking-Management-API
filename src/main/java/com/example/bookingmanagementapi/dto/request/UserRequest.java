package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private LocalDate dateOfBirth;
    private Currency currency;
}


//better to delete ToString bcs we have password field it can be shown on logs
//@ToString