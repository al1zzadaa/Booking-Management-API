package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.request.UpdatePromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.enums.DiscountType;
import com.example.bookingmanagementapi.exception.BadRequestException;
import com.example.bookingmanagementapi.exception.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface PromoCodeService {

    void create(PromoCodeRequest promoCodeResuest);

    PromoCodeResponse getById(Long id);

    List<PromoCodeResponse> getAll();

    void deleteById(Long id);

    void update(Long id, UpdatePromoCodeRequest updatePromoCodeResuest);

    PromoCodeResponse findByCode(String code);

    boolean isValid(String code);

    void activate(Long id);

    void deactivate(Long id);

//    BigDecimal discount(BigDecimal amountToWithdraw, String code);

    BigDecimal calculateFinalAmount(BigDecimal amount, String code);

    void markAsUsed(String code);

//    DiscountResult applyPromoCode(Long userId, String code);
}
