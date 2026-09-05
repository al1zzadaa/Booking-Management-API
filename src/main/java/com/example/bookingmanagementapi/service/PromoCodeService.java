package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.filter.PromoCodeFilter;
import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface PromoCodeService {

    void create(PromoCodeRequest promoCodeResuest);

    PromoCodeResponse getById(Long id);

    Page<PromoCodeResponse> getAll(PromoCodeFilter promoCodeFilter, Pageable pageable);

    void deleteById(Long id);

    void update(Long id, PromoCodeRequest updatePromoCodeResuest);

    PromoCodeResponse findByCode(String code);

    boolean isValid(String code);

    void activate(Long id);

    void deactivate(Long id);

//    BigDecimal discount(BigDecimal amountToWithdraw, String code);

    BigDecimal calculateFinalAmount(BigDecimal amount, String code);

    void markAsUsed(String code);

//    DiscountResult applyPromoCode(Long userId, String code);
}
