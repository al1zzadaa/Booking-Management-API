package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.AccountFilter;
import com.example.bookingmanagementapi.dto.request.AccountRequest;
import com.example.bookingmanagementapi.dto.request.CurrencyRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest;
import com.example.bookingmanagementapi.dto.response.AccountResponse;
import com.example.bookingmanagementapi.security.CustomUserDetails;
import com.example.bookingmanagementapi.service.AccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public void createAccount(@AuthenticationPrincipal CustomUserDetails user,
                              @RequestBody AccountRequest accountRequest) {
        accountService.create(user.getId(), accountRequest);
    }

    @DeleteMapping("/{accountId}")
    public void deleteAccount(@AuthenticationPrincipal CustomUserDetails user,
                              @PathVariable Long accountId) {

        accountService.delete(user.getId(), accountId);
    }

    @PutMapping("/{id}")
    public void updateAccount(@RequestBody UpdateAccountRequest updateAccountRequest,
                              @PathVariable Long id) {
        accountService.update(updateAccountRequest, id);
    }

    @GetMapping("/{id}")
    public AccountResponse findAccountById(@PathVariable Long id) {
        return accountService.getById(id);
    }

    @GetMapping
    public Page<@NonNull AccountResponse> findAllAccounts(AccountFilter accountFilter, Pageable pageable) {
        return accountService.getAll(accountFilter, pageable);
    }

    @PatchMapping("/block/{id}")
    public void blockAccount(@PathVariable Long id) {
        accountService.blockAccount(id);
    }

    @PatchMapping("/unblock/{id}")
    public void unblockAccount(@PathVariable Long id) {
        accountService.unblockAccount(id);
    }

    @PatchMapping("/{accountId}/currency")
    public void changeCurrency(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long accountId,
            @RequestBody CurrencyRequest request) {

        accountService.changeCurrency(user.getId(), accountId, request.getCurrency());
    }

    @GetMapping("/my")
    public List<AccountResponse> getMyAccounts(@AuthenticationPrincipal CustomUserDetails user) {
        return accountService.getMyAccounts(user.getId());
    }
}
