package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.filter.AccountFilter;
import com.example.bookingmanagementapi.dto.request.AccountRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest;
import com.example.bookingmanagementapi.dto.response.AccountResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.AccountStatus;
import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.exception.*;
import com.example.bookingmanagementapi.mapper.AccountMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.AccountService;
import com.example.bookingmanagementapi.service.ConvertService;
import com.example.bookingmanagementapi.service.specifications.AccountSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final ConvertService convertService;

    @Override
    @Transactional
    public void create(Long userId, AccountRequest request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        AccountEntity account = AccountEntity.builder()
                .user(user)
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long accountId) {

        validationUtil.validateId(accountId);

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You cannot delete another user's account"
            );
        }

        account.setStatus(AccountStatus.DELETED);
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
    public Page<AccountResponse> getAll(AccountFilter accountFilter, Pageable pageable) {

        var specification = new AccountSpecification(accountFilter);

        Page<AccountEntity> accountEntities = accountRepository.findAll(specification, pageable);

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

    @Override
    public void validateAccountCanBook(Long accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getStatus().equals(AccountStatus.DELETED)) {
            throw new AccountDeletedException("This account has been deleted");
        }

        if (account.getStatus().equals(AccountStatus.BLOCKED)) {
            throw new AccountBlockedException("This account has been blocked");
        }

        if (account.getStatus().equals(AccountStatus.INACTIVE)) {
            throw new AccountInactiveException("This account is not active");
        }
    }

    @Transactional
    @Override
    public void changeCurrency(
            Long userId,
            Long accountId,
            Currency newCurrency) {

        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You cannot modify another user's account"
            );
        }

        if (account.getCurrency() == newCurrency) {
            return;
        }

        BigDecimal convertedBalance = convertService.convert(
                account.getBalance(),
                account.getCurrency(),
                newCurrency
        );

        account.setBalance(convertedBalance);
        account.setCurrency(newCurrency);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(Long userId) {

        List<AccountEntity> accounts =
                accountRepository.findAllByUserIdAndStatus(
                        userId,
                        AccountStatus.ACTIVE
                );

        return accountMapper.toListDto(accounts);
    }

}
