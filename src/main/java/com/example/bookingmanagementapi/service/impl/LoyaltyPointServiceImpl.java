package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.LoyaltyPointRequest;
import com.example.bookingmanagementapi.dto.response.LoyaltyPointResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.LoyaltyPointEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.LoyaltyType;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.ValidationException;
import com.example.bookingmanagementapi.mapper.LoyaltyPointMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.repository.LoyaltyPointRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.LoyaltyPointService;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.support.InterceptingHttpAccessor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoyaltyPointServiceImpl implements LoyaltyPointService {

    private final LoyaltyPointRepository loyaltyPointRepository;
    private final LoyaltyPointMapper loyaltyPointMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    @Value("${loyalty.earning-rate}")
    private BigDecimal earningRate;

    @Value("${loyalty.point-value}")
    private BigDecimal pointValue;

    @Override
    public void addPoints(LoyaltyPointRequest request) {
        savePoints(request, LoyaltyType.EARN);
    }

    @Override
    public void removePoints(LoyaltyPointRequest request) {
        savePoints(request, LoyaltyType.SPEND);
    }

    private void savePoints(LoyaltyPointRequest request, LoyaltyType type) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        LoyaltyPointEntity loyaltyPointEntity = LoyaltyPointEntity.builder()
                .points(request.getPoints())
                .user(user)
                .description(request.getDescription())
                .type(type)
                .build();

        loyaltyPointRepository.save(loyaltyPointEntity);
    }

    @Override
    public Integer getPoints(Long userId) {

//        UserEntity user = userRepository.findById(userId).orElseThrow(null);

        Integer earned = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.EARN
        );

        Integer spent = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.SPEND
        );

        Integer refunded = loyaltyPointRepository.sumByUserAndType(
                userId,
                LoyaltyType.REFUND
        );

        return earned + refunded - spent;
    }

    @Override
    public List<LoyaltyPointResponse> getHistory(Long userId) {

        List<LoyaltyPointEntity> loyaltyPointEntities = loyaltyPointRepository.findAllByUserId(userId);

        return loyaltyPointMapper.toListDto(loyaltyPointEntities);
    }

    public int calculateEarnedPoints(BigDecimal amount) {
        return amount
                .multiply(earningRate)
                .setScale(0, RoundingMode.DOWN)
                .intValueExact();
    }

//    public int calculateRemovedPoints(BigDecimal amount) {
//        return amount
//                .multiply(earningRate)
//                .intValue();
//    }

    public void earnPoints(
            Long userId,
            BigDecimal amount,
            String description
    ) {

        Integer points = calculateEarnedPoints(amount);

        var loyaltyPointRequest = LoyaltyPointRequest.builder()
                .userId(userId)
                .points(points)
                .description(description)
                .build();

        addPoints(loyaltyPointRequest);
    }

    public void cancelPoints(
            Long userId,
            BigDecimal amount,
            String description
    ) {
        Integer points = calculateEarnedPoints(amount);

        var loyaltyPointRequest = LoyaltyPointRequest.builder()
                .userId(userId)
                .points(points)
                .description(description)
                .build();

        removePoints(loyaltyPointRequest);
    }


    @Override
    @Transactional
    public void usePoints(Long accountId, Integer points, String description) {

        if (points == null || points <= 0) {
            throw new ValidationException("Points must be greater than zero");
        }

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        UserEntity user = account.getUser();

        Integer availablePoints = getPoints(user.getId());

        if (points > availablePoints) {
            throw new ValidationException("Not enough loyalty points");
        }

        LoyaltyPointEntity loyaltyPoint = LoyaltyPointEntity.builder()
                .user(user)
                .points(points)
                .type(LoyaltyType.USE)
                .description(description)
                .build();

        loyaltyPointRepository.save(loyaltyPoint);
    }
}
