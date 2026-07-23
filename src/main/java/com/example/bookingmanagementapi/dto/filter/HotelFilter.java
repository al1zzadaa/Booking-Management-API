package com.example.bookingmanagementapi.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelFilter {
    private Integer fromRating;
    private Integer toRating;
    private String name;
    private List<String> city;
    private List<String> country;
    private Integer fromDistant;
    private Integer toDistant;
}
