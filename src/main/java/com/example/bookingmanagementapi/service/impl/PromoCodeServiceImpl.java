package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.filter.PromoCodeFilter;
import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.request.UpdatePromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.enums.DiscountType;
import com.example.bookingmanagementapi.exception.BadRequestException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.PromoCodeMapper;
import com.example.bookingmanagementapi.repository.PromoCodeRepository;
import com.example.bookingmanagementapi.service.PromoCodeService;
import com.example.bookingmanagementapi.service.UserPromoCodeService;
import com.example.bookingmanagementapi.service.specifications.PromoCodeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeMapper promoCodeMapper;
    private final PromoCodeRepository promoCodeRepository;
    private final UserPromoCodeService userPromoCodeService;

    @Override
    public void create(PromoCodeRequest promoCodeResuest) {
        PromoCodeEntity promoCodeEntity = promoCodeMapper.toEntity(promoCodeResuest);

        promoCodeRepository.save(promoCodeEntity);
    }

    @Override
    public PromoCodeResponse getById(Long id) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository
                .findById(id).orElseThrow(null);

        return promoCodeMapper.toDto(promoCodeEntity);
    }

    @Override
    public Page<PromoCodeResponse> getAll(PromoCodeFilter promoCodeFilter, Pageable pageable) {

        var specification = new PromoCodeSpecification(promoCodeFilter);

        Page<PromoCodeEntity> promoCodeEntities = promoCodeRepository.findAll(specification, pageable);

        return promoCodeEntities.map(promoCodeMapper::toDto);
    }

    @Override
    public void deleteById(Long id) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository
                .findById(id).orElseThrow(null);

        promoCodeRepository.delete(promoCodeEntity);
    }

    @Override
    public void update(Long id, UpdatePromoCodeRequest updatePromoCodeResuest) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository
                .findById(id).orElseThrow(null);

        promoCodeMapper.update(updatePromoCodeResuest, promoCodeEntity);

        promoCodeRepository.save(promoCodeEntity);
    }

    @Override
    public PromoCodeResponse findByCode(String code) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(code).orElseThrow();

        return promoCodeMapper.toDto(promoCodeEntity);
    }

    @Override
    public boolean isValid(String code) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(code).orElseThrow();

        if (promoCodeEntity.getUsedCount().equals(promoCodeEntity.getUsageLimit())) {
            return false;
        }

        return true;
    }

    @Override
    public void activate(Long id) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository.findById(id)
                .orElseThrow(null);

        promoCodeEntity.setActive(true);

        promoCodeRepository.save(promoCodeEntity);
    }

    @Override
    public void deactivate(Long id) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository.findById(id)
                .orElseThrow(null);

        promoCodeEntity.setActive(false);

        promoCodeRepository.save(promoCodeEntity);
    }

    //    @Override
//    public BigDecimal discount(BigDecimal amountToWithdraw, String code) {
//
//        if (code == null || code.isBlank()) {
//            return amountToWithdraw;
//        }
//
//        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(code).orElseThrow();
//
//        if (!promoCodeEntity.getActive()) {
//            throw new BadRequestException("Promo code is inactive");
//        }
//
//        if (promoCodeEntity.getEndDate().isBefore(LocalDateTime.now())) {
//            throw new BadRequestException("Promo code has expired");
//        }
//
//        BigDecimal discountValue = promoCodeEntity.getDiscountValue();
//
//
//        if (promoCodeEntity.getDiscountType() == DiscountType.PERCENTAGE) {
//            if (discountValue.compareTo(BigDecimal.ZERO) < 0
//                    || discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
//                throw new BadRequestException("Invalid percentage discount");
//            }
//
//            discountValue = amountToWithdraw
//                    .multiply(discountValue)
//                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//        } else {
//            discountValue = discountValue.min(amountToWithdraw);
//        }
//
//        BigDecimal finalAmount = amountToWithdraw.subtract(discountValue);
//
//        promoCodeEntity.setUsedCount(promoCodeEntity.getUsedCount() + 1);
//        promoCodeRepository.save(promoCodeEntity);
//
//        return finalAmount;
//    }
    @Override
    public BigDecimal calculateFinalAmount(BigDecimal amount, String code) {

        if (code == null || code.isBlank()) {
            return amount;
        }

        PromoCodeEntity promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Promo code not found"));

        validatePromoCode(promoCode);

        BigDecimal discount;

        if (promoCode.getDiscountType() == DiscountType.PERCENTAGE) {

            BigDecimal percent = promoCode.getDiscountValue();

            if (percent.compareTo(BigDecimal.ZERO) < 0
                    || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Invalid percentage discount");
            }

            discount = amount.multiply(percent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        } else {

            discount = promoCode.getDiscountValue().min(amount);
        }

        return amount.subtract(discount);
    }

    @Override
    public void markAsUsed(String code) {

        if (code == null || code.isBlank()) {
            return;
        }

        PromoCodeEntity promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Promo code not found"));

        if (promoCode.getUsageLimit() != null
                && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw new BadRequestException("Promo code usage limit exceeded");
        }

        promoCode.setUsedCount(promoCode.getUsedCount() + 1);
    }

    private void validatePromoCode(PromoCodeEntity promoCode) {

        if (!promoCode.getActive()) {
            throw new BadRequestException("Promo code is inactive");
        }

        if (promoCode.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Promo code has expired");
        }

        if (promoCode.getUsageLimit() != null
                && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw new BadRequestException("Promo code usage limit exceeded");
        }

//        if (!isValid(promoCode.getCode())) {
//            throw new BadRequestException("Invalid promo code");
//        }
    }

}
