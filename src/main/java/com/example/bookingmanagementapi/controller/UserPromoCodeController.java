package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.response.UserPromoCodeResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.UserPromoCodeService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-promo-codes")
@RequiredArgsConstructor
public class UserPromoCodeController {

    private final UserPromoCodeService userPromoCodeService;

    @GetMapping
    public Page<@NonNull UserPromoCodeResponse> getMyPromoCodes(
            @AuthenticationPrincipal CustomUserDetails user,
            Pageable pageable) {
        return userPromoCodeService.getUserPromoCodes(
                user.getId(),
                pageable
        );
    }

}
