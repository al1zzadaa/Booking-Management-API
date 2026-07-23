package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.service.UserPromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-promo-codes")
@RequiredArgsConstructor
public class UserPromoCodeController {

    private final UserPromoCodeService userPromoCodeService;


}
