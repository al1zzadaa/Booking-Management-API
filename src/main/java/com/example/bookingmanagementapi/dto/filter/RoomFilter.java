package com.example.bookingmanagementapi.dto.filter;

import com.example.bookingmanagementapi.enums.Rooms;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomFilter {
    private String roomName;
    private List<Rooms> roomType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minCapacity;
    private Integer maxCapacity;
}
