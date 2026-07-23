package com.example.bookingmanagementapi.dto.response.hotel;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoomResponse{
    private String id;
    private Long hotelId;
    private Integer roomNumber;
    private String roomName;
    private String roomType;
    private BigDecimal pricePerNight;
    private Integer roomCapacity;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
