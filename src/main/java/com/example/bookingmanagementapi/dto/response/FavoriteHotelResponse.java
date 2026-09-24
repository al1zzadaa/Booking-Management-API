package com.example.bookingmanagementapi.dto.response;

import com.example.bookingmanagementapi.dto.response.hotel.HotelResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteHotelResponse {
    private Long id;
    private Long hotelId;
    private Long userId;
    private HotelResponse hotel;
}
