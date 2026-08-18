package com.example.bookingmanagementapi.service;

import com.example.bookingmanagementapi.dto.filter.AccountFilter;
import com.example.bookingmanagementapi.dto.request.AccountRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest;
import com.example.bookingmanagementapi.dto.response.AccountResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.Currency;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    void create(AccountRequest accountRequest);

    void delete(Long id);

    void update(UpdateAccountRequest updateAccountRequest, Long id);

    AccountResponse getById(Long id);

    Page<@NonNull AccountResponse> getAll(AccountFilter accountFilter, Pageable pageable);

    void blockAccount(Long id);

    void unblockAccount(Long id);

    AccountEntity createDefaultAccount(UserEntity user, Currency currency);

    void validateAccountCanBook(Long accountId);
}
