package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.filter.AccountFilter;
import com.example.bookingmanagementapi.dto.request.AccountRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest;
import com.example.bookingmanagementapi.dto.response.AccountResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.AccountStatus;
import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.AccountMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.service.AccountService;
import com.example.bookingmanagementapi.service.specifications.AccountSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ValidationUtil validationUtil;

    @Override
    public void create(AccountRequest accountRequest) {
        accountRepository.save(accountMapper.toEntity(accountRequest));
    }

    @Override
    public void delete(Long id) {

        validationUtil.validateId(id);

        if(!accountRepository.existsById(id)){
            throw new NotFoundException("Account not found");
        }
        accountRepository.deleteById(id);
    }

    @Override
    public void update(UpdateAccountRequest updateAccountRequest, Long id) {

        validationUtil.validateId(id);

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        accountMapper.updateAccount(updateAccountRequest, accountEntity);

        accountRepository.save(accountEntity);
    }

    @Override
    public AccountResponse getById(Long id) {

        validationUtil.validateId(id);

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        return accountMapper.toDto(accountEntity);
    }

    @Override
    public Page<AccountResponse> getAll(AccountFilter accountFilter,  Pageable pageable) {

        var specification = new AccountSpecification(accountFilter);

        Page<AccountEntity> accountEntities = accountRepository.findAll(specification,  pageable);

        return accountEntities.map(accountMapper::toDto);
    }

    @Override
    public void blockAccount(Long id) {

        validationUtil.validateId(id);

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

       accountEntity.setStatus(AccountStatus.BLOCKED);
    }

    @Override
    public void unblockAccount(Long id) {

        validationUtil.validateId(id);

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        accountEntity.setStatus(AccountStatus.ACTIVE);
    }

    @Override
    public AccountEntity createDefaultAccount(UserEntity user, Currency currency) {
        return AccountEntity.builder()
                .user(user)
                .currency(currency)
                .build();

    }

}
