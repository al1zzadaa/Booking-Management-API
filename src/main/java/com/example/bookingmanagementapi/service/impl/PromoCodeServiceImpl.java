package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.filter.PromoCodeFilter;
import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.enums.DiscountType;
import com.example.bookingmanagementapi.exception.BadRequestException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.PromoCodeMapper;
import com.example.bookingmanagementapi.repository.PromoCodeRepository;
import com.example.bookingmanagementapi.service.PromoCodeService;
import com.example.bookingmanagementapi.service.specifications.PromoCodeSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeMapper promoCodeMapper;
    private final PromoCodeRepository promoCodeRepository;
    private final ValidationUtil validationUtil;

    @Override
    public void create(PromoCodeRequest promoCodeResuest) {

        validationUtil.checkTime(promoCodeResuest.getStartDate(), promoCodeResuest.getEndDate());

        PromoCodeEntity promoCodeEntity = promoCodeMapper.toEntity(promoCodeResuest);

        promoCodeRepository.save(promoCodeEntity);
    }

    @Override
    public PromoCodeResponse getById(Long id) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository
                .findById(id).orElseThrow(() -> new NotFoundException("PromoCode not found"));

        return promoCodeMapper.toDto(promoCodeEntity);
    }

    @Override
    public Page<@NonNull PromoCodeResponse> getAll(PromoCodeFilter promoCodeFilter, Pageable pageable) {

        var specification = new PromoCodeSpecification(promoCodeFilter);

        Page<@NonNull PromoCodeEntity> promoCodeEntities = promoCodeRepository.findAll(specification, pageable);

        return promoCodeEntities.map(promoCodeMapper::toDto);
    }


    @Override
    public void deleteById(Long id) {
        var promoCodeEntity = getPromoCodeEntity(id);

        promoCodeRepository.delete(promoCodeEntity);
    }

    @Override
    public void update(Long id, PromoCodeRequest updatePromoCodeResuest) {
        var promoCodeEntity = getPromoCodeEntity(id);

        promoCodeMapper.update(updatePromoCodeResuest, promoCodeEntity);

        promoCodeRepository.save(promoCodeEntity);
    }

    @Override
    public PromoCodeResponse findByCode(String code) {
        var promoCodeEntity = getPromoCodeEntity(code);

        return promoCodeMapper.toDto(promoCodeEntity);
    }

    @Override
    public boolean isValid(String code) {
        var promoCodeEntity = getPromoCodeEntity(code);

        return !promoCodeEntity.getUsedCount().equals(promoCodeEntity.getUsageLimit());
    }

    @Override
    public void activate(Long id) {
        var promoCodeEntity = getPromoCodeEntity(id);

        promoCodeEntity.setActive(true);

        promoCodeRepository.save(promoCodeEntity);

        log.info("Promo code {} activated", promoCodeEntity.getId());
    }

    @Override
    public void deactivate(Long id) {
        var promoCodeEntity = getPromoCodeEntity(id);

        promoCodeEntity.setActive(false);

        promoCodeRepository.save(promoCodeEntity);

        log.info("Promo code {} deactivated", promoCodeEntity.getId());
    }

    @Override
    public BigDecimal calculateFinalAmount(BigDecimal amount, String code) {

        if (code == null || code.isBlank()) {
            return amount;
        }

        var promoCodeEntity = getPromoCodeEntity(code);

        validatePromoCode(promoCodeEntity);

        BigDecimal discount;

        if (promoCodeEntity.getDiscountType() == DiscountType.PERCENTAGE) {

            BigDecimal percent = promoCodeEntity.getDiscountValue();

            if (percent.compareTo(BigDecimal.ZERO) < 0
                    || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Invalid percentage discount");
            }

            discount = amount.multiply(percent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        } else {

            discount = promoCodeEntity.getDiscountValue().min(amount);
        }

        return amount.subtract(discount);
    }

    @Override
    public void markAsUsed(String code) {

        if (code == null || code.isBlank()) {
            return;
        }

        var promoCodeEntity = getPromoCodeEntity(code);


        if (promoCodeEntity.getUsageLimit() != null
                && promoCodeEntity.getUsedCount() >= promoCodeEntity.getUsageLimit()) {
            throw new BadRequestException("Promo code usage limit exceeded");
        }

        promoCodeEntity.setUsedCount(promoCodeEntity.getUsedCount() + 1);
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
    }

    private PromoCodeEntity getPromoCodeEntity(Long id) {
        return promoCodeRepository.findById(id).orElseThrow(() -> new NotFoundException("PromoCode not found"));
    }

    private PromoCodeEntity getPromoCodeEntity(String code) {
        return promoCodeRepository.findByCode(code).orElseThrow(() -> new NotFoundException("PromoCode not found"));
    }

}
