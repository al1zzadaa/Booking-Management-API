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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
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

        log.info("Account created: accountId={}, userId={}, currency={}",
                account.getId(),
                user.getId(),
                account.getCurrency()
        );
    }

    @Override
    @Transactional
    public void delete(Long userId, Long accountId) {

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "You cannot delete another user's account"
            );
        }

        account.setStatus(AccountStatus.DELETED);

        log.info("Account '{}' deleted", accountId);
    }

    @Override
    public void update(UpdateAccountRequest updateAccountRequest, Long id) {

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        accountMapper.updateAccount(updateAccountRequest, accountEntity);

        accountRepository.save(accountEntity);

        log.info("Account '{}' updated with request '{}'", accountEntity, updateAccountRequest);
    }

    @Override
    public AccountResponse getById(Long id) {

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        return accountMapper.toDto(accountEntity);
    }

    @Override
    public Page<@NonNull AccountResponse> getAll(AccountFilter accountFilter, Pageable pageable) {

        var specification = new AccountSpecification(accountFilter);

        Page<@NonNull AccountEntity> accountEntities = accountRepository.findAll(specification, pageable);

        return accountEntities.map(accountMapper::toDto);
    }

    @Transactional
    @Override
    public void blockAccount(Long id) {


        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (accountEntity.getStatus().equals(AccountStatus.BLOCKED)) {
            throw new AccountBlockedException("Account is blocked");
        }

        accountEntity.setStatus(AccountStatus.BLOCKED);

        log.info("Account '{}' blocked", accountEntity.getId());
    }

    @Transactional
    @Override
    public void unblockAccount(Long id) {

        AccountEntity accountEntity = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        accountEntity.setStatus(AccountStatus.ACTIVE);

        log.info("Account '{}' unblocked", accountEntity.getId());
    }

    @Transactional
    @Override
    public AccountEntity createDefaultAccount(UserEntity user, Currency currency) {
        log.info("Creating default account for user '{}'", user.getEmail());
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

        log.info("Account '{}' changed currency '{}'", accountId, newCurrency);
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
