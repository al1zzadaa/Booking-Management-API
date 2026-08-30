package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.response.UserPromoCodeResponse;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.entity.UserPromoCodeEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.repository.PromoCodeRepository;
import com.example.bookingmanagementapi.repository.UserPromoCodeRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.UserPromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPromoCodeServiceImpl implements UserPromoCodeService {

    private final UserRepository userRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final UserPromoCodeRepository userPromoCodeRepository;

    @Override
    public void applyPromoCode(Long userId, String promoCode) {

        if (promoCode == null || promoCode.isBlank()) {
            return;
        }

        UserEntity user =  userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Promo code not found"));

        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(promoCode).orElseThrow();

        //TODO notification
        userPromoCodeRepository.save(
                UserPromoCodeEntity.builder()
                        .user(user)
                        .promoCode(promoCodeEntity)
                        .usedAt(LocalDateTime.now())
                        .build()
        );

        log.info("Applying promo code {} to user {}", promoCode, user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserPromoCodeResponse> getUserPromoCodes(
            Long userId,
            Pageable pageable
    ) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return userPromoCodeRepository
                .findAllByUserId(userId, pageable)
                .map(userPromoCode -> UserPromoCodeResponse.builder()
                        .id(userPromoCode.getId())
                        .userId(userPromoCode.getUser().getId())
                        .planId(userPromoCode.getPromoCode().getId())
                        .usedAt(LocalDateTime.now())
                        .build()
                );
    }
}
