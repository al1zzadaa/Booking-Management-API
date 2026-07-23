package com.example.bookingmanagementapi.dto.request;


import com.example.bookingmanagementapi.enums.Rooms;

import java.math.BigDecimal;

public class RoomRequest {
    private Long hotelId;
    private Integer roomNumber;
    private String roomName;
    private Rooms roomType;
    private BigDecimal pricePerNight;
    private Integer roomCapacity;
    private String description;
}
