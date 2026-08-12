package com.example.bookingmanagementapi.dto.request;

import com.example.bookingmanagementapi.enums.PassengerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerRequest {

    private PassengerType type;
    private Integer age;
    private Long seatId;
}