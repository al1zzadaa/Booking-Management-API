package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.PromoCodeRequest;
import com.example.bookingmanagementapi.dto.request.UpdatePromoCodeRequest;
import com.example.bookingmanagementapi.dto.response.PromoCodeResponse;
import com.example.bookingmanagementapi.entity.PromoCodeEntity;
import com.example.bookingmanagementapi.mapper.PromoCodeMapper;
import com.example.bookingmanagementapi.repository.PromoCodeRepository;
import com.example.bookingmanagementapi.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeMapper promoCodeMapper;
    private final PromoCodeRepository promoCodeRepository;

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
    public List<PromoCodeResponse> getAll() {
        List<PromoCodeEntity> promoCodeEntities = promoCodeRepository.findAll();

        return promoCodeMapper.toDto(promoCodeEntities);
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
        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(code);

        return promoCodeMapper.toDto(promoCodeEntity);
    }

    @Override
    public boolean isValid(String code) {
        PromoCodeEntity promoCodeEntity = promoCodeRepository.findByCode(code);

        if (promoCodeEntity.getUsedCount().equals(promoCodeEntity.getUsageLimit())){
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
}
