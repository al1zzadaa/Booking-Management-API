package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.response.UserPromoCodeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPromoCodeService {

    void applyPromoCode(Long userId, String promoCode);

    Page<UserPromoCodeResponse> getUserPromoCodes(
            Long userId,
            Pageable pageable
    );
}
