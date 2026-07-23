package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.entity.UserPromoCodeEntity;
import com.example.bookingmanagementapi.repository.PromoCodeRepository;
import com.example.bookingmanagementapi.repository.UserPromoCodeRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.UserPromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserPromoCodeServiceImpl implements UserPromoCodeService {

    private final UserRepository userRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final NotificationService notificationService;
    private final UserPromoCodeRepository userPromoCodeRepository;

    @Override
    public void applyPromoCode(Long userId, String promoCode) {

        UserEntity user =  userRepository.findById(userId).orElseThrow();

        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(promoCode);

        //TODO notification

        UserPromoCodeEntity userPromoCodeEntity = new UserPromoCodeEntity();
        userPromoCodeEntity.setUser(user);
        userPromoCodeEntity.setPromoCode(promoCodeEntity);
        userPromoCodeEntity.setUsedAt(LocalDateTime.now());

        userPromoCodeRepository.save(userPromoCodeEntity);
    }
}
