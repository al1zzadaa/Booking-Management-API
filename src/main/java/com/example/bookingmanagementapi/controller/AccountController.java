package com.example.bookingmanagementapi.controller;

import com.example.bookingmanagementapi.dto.filter.AccountFilter;
import com.example.bookingmanagementapi.dto.request.AccountRequest;
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest;
import com.example.bookingmanagementapi.dto.response.AccountResponse;
import com.example.bookingmanagementapi.service.AccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public void createAccount(@RequestBody AccountRequest accountRequest) {
        accountService.create(accountRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.delete(id);
    }

    @PutMapping("/{id}")
    public void updateAccount(@RequestBody UpdateAccountRequest updateAccountRequest, @PathVariable Long id) {
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
}
