package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.AccountFilter
import com.example.bookingmanagementapi.dto.request.AccountRequest
import com.example.bookingmanagementapi.dto.request.UpdateAccountRequest
import com.example.bookingmanagementapi.dto.response.AccountResponse
import com.example.bookingmanagementapi.entity.AccountEntity
import com.example.bookingmanagementapi.entity.UserEntity
import java.time.LocalDate
import com.example.bookingmanagementapi.enums.AccountStatus
import com.example.bookingmanagementapi.enums.Currency
import com.example.bookingmanagementapi.exception.AccessDeniedException
import com.example.bookingmanagementapi.exception.AccountBlockedException
import com.example.bookingmanagementapi.exception.AccountDeletedException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.mapper.AccountMapper
import com.example.bookingmanagementapi.repository.AccountRepository
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.AccountServiceImpl
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import spock.lang.Specification

class AccountServiceTest extends Specification {

    def accountRepository = Mock(AccountRepository)
    def accountMapper = Mock(AccountMapper)
    def userRepository = Mock(UserRepository)
    def convertService = Mock(ConvertService)

    def accountService = new AccountServiceImpl(
            accountRepository,
            accountMapper,
            userRepository,
            convertService
    )


    def "create should create account successfully"() {
        given:
        Long userId = 1L

        def request = new AccountRequest()
        request.setCurrency(Currency.AZN)

        def user = new UserEntity()
        user.setId(userId)

        def account = AccountEntity.builder()
                .user(user)
                .currency(Currency.AZN)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build()

        when:
        accountService.create(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * accountRepository.save({
            it.user == user &&
                    it.currency == Currency.AZN &&
                    it.balance == BigDecimal.ZERO &&
                    it.status == AccountStatus.ACTIVE
        })
    }


    def "create should throw NotFoundException when user does not exist"() {
        given:
        Long userId = 1L
        def request = new AccountRequest()
        request.setCurrency(Currency.AZN)

        when:
        accountService.create(userId, request)

        then:
        1 * userRepository.findById(userId) >> Optional.empty()
        0 * accountRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "User not found"
    }


    def "delete should mark account as deleted"() {
        given:
        Long userId = 1L
        Long accountId = 10L

        def user = new UserEntity()
        user.setId(userId)

        def account = new AccountEntity()
        account.setId(accountId)
        account.setUser(user)
        account.setStatus(AccountStatus.ACTIVE)

        when:
        accountService.delete(userId, accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        account.status == AccountStatus.DELETED
    }


    def "delete should throw NotFoundException when account does not exist"() {
        given:
        Long userId = 1L
        Long accountId = 10L

        when:
        accountService.delete(userId, accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "Account not found"
    }


    def "delete should throw AccessDeniedException when account belongs to another user"() {
        given:
        Long userId = 1L
        Long ownerId = 2L
        Long accountId = 10L

        def user = new UserEntity()
        user.setId(ownerId)

        def account = new AccountEntity()
        account.setId(accountId)
        account.setUser(user)
        account.setStatus(AccountStatus.ACTIVE)

        when:
        accountService.delete(userId, accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        thrown(AccessDeniedException)

        account.status == AccountStatus.ACTIVE
    }


    def "update should update account successfully"() {
        given:
        Long accountId = 10L

        def request = new UpdateAccountRequest()
        def account = new AccountEntity()

        when:
        accountService.update(request, accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)
        1 * accountMapper.updateAccount(request, account)
        1 * accountRepository.save(account)
    }


    def "update should throw NotFoundException when account does not exist"() {
        given:
        Long accountId = 10L
        def request = new UpdateAccountRequest()

        when:
        accountService.update(request, accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.empty()

        0 * accountMapper.updateAccount(_, _)
        0 * accountRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "Account not found"
    }


    def "getById should return account successfully"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        def response = new AccountResponse()

        when:
        def result = accountService.getById(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)
        1 * accountMapper.toDto(account) >> response

        result == response
    }


    def "getById should throw NotFoundException when account does not exist"() {
        given:
        Long accountId = 10L

        when:
        accountService.getById(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.empty()
        0 * accountMapper.toDto(_)

        def exception = thrown(NotFoundException)
        exception.message == "Account not found"
    }


    def "getAll should return accounts successfully"() {
        given:
        def filter = new AccountFilter()
        filter.setUserId(10L);
        filter.setMinBalance(new BigDecimal("100.00"));
        filter.setMaxBalance(new BigDecimal("5000.00"));
        filter.setCurrency("AZN");
        filter.setActive(true);
        filter.setCreatedFrom(LocalDate.of(2026, 1, 1));
        filter.setCreatedTo(LocalDate.of(2026, 9, 20));

        def pageable = PageRequest.of(0, 10)

        def account1 = new AccountEntity()
        def account2 = new AccountEntity()

        def response1 = new AccountResponse()
        def response2 = new AccountResponse()

        def page = new PageImpl<AccountEntity>(
                [account1, account2],
                pageable,
                2
        )

        when:
        Page<AccountResponse> result =
                accountService.getAll(filter, pageable)

        then:
        1 * accountRepository.findAll(_, pageable) >> page

        1 * accountMapper.toDto(account1) >> response1
        1 * accountMapper.toDto(account2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
        result.number == 0
        result.size == 10
    }


    def "getAll should return empty page when no accounts exist"() {
        given:
        def filter = new AccountFilter()
        filter.setUserId(10L)
        filter.setMinBalance(new BigDecimal("100.00"))
        filter.setMaxBalance(new BigDecimal("5000.00"))
        filter.setCurrency("AZN")
        filter.setActive(true)
        filter.setCreatedFrom(LocalDate.of(2026, 1, 1))
        filter.setCreatedTo(LocalDate.of(2026, 9, 20))

        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<AccountEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = accountService.getAll(filter, pageable)

        then:
        1 * accountRepository.findAll(_, pageable) >> page
        0 * accountMapper.toDto(_)

        result.empty
        result.totalElements == 0
    }


    def "blockAccount should block active account"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        account.setId(accountId)
        account.setStatus(AccountStatus.ACTIVE)

        when:
        accountService.blockAccount(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        account.status == AccountStatus.BLOCKED
    }


    def "blockAccount should throw AccountBlockedException when account is already blocked"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        account.setId(accountId)
        account.setStatus(AccountStatus.BLOCKED)

        when:
        accountService.blockAccount(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        thrown(AccountBlockedException)

        account.status == AccountStatus.BLOCKED
    }


    def "blockAccount should throw NotFoundException when account does not exist"() {
        given:
        Long accountId = 10L

        when:
        accountService.blockAccount(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.empty()

        thrown(NotFoundException)
    }


    def "unblockAccount should activate account"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        account.setId(accountId)
        account.setStatus(AccountStatus.BLOCKED)

        when:
        accountService.unblockAccount(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        account.status == AccountStatus.ACTIVE
    }


    def "unblockAccount should throw NotFoundException when account does not exist"() {
        given:
        Long accountId = 10L

        when:
        accountService.unblockAccount(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.empty()

        thrown(NotFoundException)
    }


    def "createDefaultAccount should create account with user and currency"() {
        given:
        def user = new UserEntity()
        def currency = Currency.AZN

        when:
        def result = accountService.createDefaultAccount(user, currency)

        then:
        result.user == user
        result.currency == currency
    }


    def "validateAccountCanBook should pass for active account"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        account.setStatus(AccountStatus.ACTIVE)

        when:
        accountService.validateAccountCanBook(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)
        noExceptionThrown()
    }


    def "validateAccountCanBook should throw AccountDeletedException for deleted account"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        account.setStatus(AccountStatus.DELETED)

        when:
        accountService.validateAccountCanBook(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        thrown(AccountDeletedException)
    }


    def "validateAccountCanBook should throw AccountBlockedException for blocked account"() {
        given:
        Long accountId = 10L

        def account = new AccountEntity()
        account.setStatus(AccountStatus.BLOCKED)

        when:
        accountService.validateAccountCanBook(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.of(account)

        thrown(AccountBlockedException)
    }


    def "validateAccountCanBook should throw NotFoundException when account does not exist"() {
        given:
        Long accountId = 10L

        when:
        accountService.validateAccountCanBook(accountId)

        then:
        1 * accountRepository.findById(accountId) >> Optional.empty()

        thrown(NotFoundException)
    }


    def "changeCurrency should convert balance and change currency"() {
        given:
        Long userId = 1L
        Long accountId = 10L

        def user = new UserEntity()
        user.setId(userId)

        def account = new AccountEntity()
        account.setId(accountId)
        account.setUser(user)
        account.setCurrency(Currency.USD)
        account.setBalance(new BigDecimal("100.00"))

        def convertedBalance = new BigDecimal("170.00")

        when:
        accountService.changeCurrency(
                userId,
                accountId,
                Currency.AZN
        )

        then:
        1 * accountRepository.findByIdForUpdate(accountId) >> Optional.of(account)

        1 * convertService.convert(
                new BigDecimal("100.00"),
                Currency.USD,
                Currency.AZN
        ) >> convertedBalance

        account.balance == convertedBalance
        account.currency == Currency.AZN
    }


    def "changeCurrency should throw NotFoundException when account does not exist"() {
        given:
        Long userId = 1L
        Long accountId = 10L

        when:
        accountService.changeCurrency(
                userId,
                accountId,
                Currency.AZN
        )

        then:
        1 * accountRepository.findByIdForUpdate(accountId) >> Optional.empty()

        0 * convertService.convert(_, _, _)

        thrown(NotFoundException)
    }


    def "changeCurrency should throw AccessDeniedException when account belongs to another user"() {
        given:
        Long userId = 1L
        Long ownerId = 2L
        Long accountId = 10L

        def user = new UserEntity()
        user.setId(ownerId)

        def account = new AccountEntity()
        account.setId(accountId)
        account.setUser(user)
        account.setCurrency(Currency.USD)
        account.setBalance(new BigDecimal("100.00"))

        when:
        accountService.changeCurrency(
                userId,
                accountId,
                Currency.AZN
        )

        then:
        1 * accountRepository.findByIdForUpdate(accountId) >> Optional.of(account)

        0 * convertService.convert(_, _, _)

        thrown(AccessDeniedException)

        account.currency == Currency.USD
        account.balance == new BigDecimal("100.00")
    }


    def "changeCurrency should do nothing when currency is already the same"() {
        given:
        Long userId = 1L
        Long accountId = 10L

        def user = new UserEntity()
        user.setId(userId)

        def account = new AccountEntity()
        account.setUser(user)
        account.setCurrency(Currency.AZN)
        account.setBalance(new BigDecimal("100.00"))

        when:
        accountService.changeCurrency(
                userId,
                accountId,
                Currency.AZN
        )

        then:
        1 * accountRepository.findByIdForUpdate(accountId) >> Optional.of(account)

        0 * convertService.convert(_, _, _)

        account.currency == Currency.AZN
        account.balance == new BigDecimal("100.00")
    }


    def "getMyAccounts should return user's active accounts"() {
        given:
        Long userId = 1L

        def account1 = new AccountEntity()
        def account2 = new AccountEntity()

        def response1 = new AccountResponse()
        def response2 = new AccountResponse()

        def accounts = [account1, account2]
        def responses = [response1, response2]

        when:
        def result = accountService.getMyAccounts(userId)

        then:
        1 * accountRepository.findAllByUserIdAndStatus(
                userId,
                AccountStatus.ACTIVE
        ) >> accounts

        1 * accountMapper.toListDto(accounts) >> responses

        result == responses
    }
}
