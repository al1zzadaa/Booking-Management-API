package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TransactionRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTransactionRequest;
import com.example.bookingmanagementapi.dto.request.WithdrawRequest;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.TicketEntity;
import com.example.bookingmanagementapi.entity.TransactionEntity;
import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import com.example.bookingmanagementapi.exception.InsufficientBalanceException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.TransactionMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.repository.TransactionRepository;
import com.example.bookingmanagementapi.service.TransactionService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ValidationUtil validationUtil;
    private final AccountRepository accountRepository;


    @Override
    public void createTransaction(TransactionRequest transactionRequest) {
        TransactionEntity transactionEntity = transactionMapper.toEntity(transactionRequest);
        transactionRepository.save(transactionEntity);
    }

    @Override
    public void updateTransaction(UpdateTransactionRequest updateTransactionRequest, Long transactionId) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionId).orElseThrow(null);

        transactionMapper.updateTransaction(updateTransactionRequest, transactionEntity);

        transactionRepository.save(transactionEntity);
    }

    @Override
    public void deleteTransaction(Long transactionId) {

        TransactionEntity transactionEntity = transactionRepository.findById(transactionId).orElseThrow(null);

        transactionRepository.delete(transactionEntity);
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionId).orElseThrow(null);

        return transactionMapper.toDto(transactionEntity);
    }

    @Override
    public Page<@NonNull TransactionResponse> getTransactionsByUserId(Long accountId, Pageable pageable) {


        AccountEntity accountEntity = transactionRepository.findById(accountId).orElseThrow(null).getAccount();

        Page<@NonNull TransactionEntity> responses = transactionRepository.findAllByAccount(accountEntity, pageable);

        return responses.map(transactionMapper::toDto);
    }

    @Transactional
    @Override
    public void payForTicket(TicketEntity ticket, PaymentRequest paymentRequest) {

        AccountEntity accountEntity = accountRepository.findById(paymentRequest.getAccountId())
                .orElseThrow(null);

        BigDecimal balance = accountEntity.getBalance();

        BigDecimal amountToWithdraw =
                accountEntity.getCurrency() == Currency.USD
                        ? ticket.getPrice()
                        : convertToUsd(ticket.getPrice(), accountEntity.getCurrency());


        if (balance.compareTo(amountToWithdraw) < 0) {
            //TODO
            throw new InsufficientBalanceException("Not enough balance");
        }

        accountEntity.setBalance(
                accountEntity.getBalance().subtract(amountToWithdraw)
        );

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .amount(amountToWithdraw)
                .account(accountEntity)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceType(ReferenceType.FLIGHT_TICKET)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .type(TransactionType.PAYMENT)
                .description("Payment for ticket")
                .referenceId(ticket.getId())
                .build();

//        withdraw(ticket.getPrice());

        transactionRepository.save(transactionEntity);
    }


//    private BigDecimal convertAmount(BigDecimal price, Currency currency) {
//        return switch (currency) {
//            case AZN -> price.divide(BigDecimal.valueOf(1.7), 2, RoundingMode.HALF_UP);
//            case EUR -> price.multiply(BigDecimal.valueOf(1.14)).setScale(2, RoundingMode.HALF_UP);
//            default -> BigDecimal.ZERO;
//        };
//    }

    private BigDecimal convertToUsd(BigDecimal amount, Currency currency) {
        return switch (currency) {
            case USD -> amount;
            case AZN -> amount.divide(BigDecimal.valueOf(1.7), 2, RoundingMode.HALF_UP);
            case EUR -> amount.multiply(BigDecimal.valueOf(1.14)).setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal convertFromUsd(BigDecimal amount, Currency currency) {
        return switch (currency) {
            case USD -> amount;
            case AZN -> amount.multiply(BigDecimal.valueOf(1.7)).setScale(2, RoundingMode.HALF_UP);
            case EUR -> amount.divide(BigDecimal.valueOf(1.14), 2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };
    }

    @Transactional
    public void refund(TicketEntity ticket, BigDecimal amount) {
        AccountEntity account = ticket.getAccount();

        BigDecimal refundAmount = amount;

        if (account.getCurrency() != Currency.USD) {
            refundAmount = convertToUsd(amount, account.getCurrency());
        }

        account.setBalance(account.getBalance().add(refundAmount));

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(ticket.getAccount())
                .amount(refundAmount)
                .type(TransactionType.REFUND)
                .description("Refund")
                .referenceId(ticket.getId())
                .paymentStatus(PaymentStatus.REFUNDED)
                .referenceType(ReferenceType.FLIGHT_TICKET)
                .build();

        transactionRepository.save(transactionEntity);
    }

    @Override
    @Transactional()
    public void withdraw(WithdrawRequest withdrawRequest) {

        validationUtil.validateId(withdrawRequest.getAccountId());

        AccountEntity accountEntity = accountRepository.findById(withdrawRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (accountEntity.getBalance().compareTo(withdrawRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException("Not enough balance to  withdraw");
        }

        convertToUsd(withdrawRequest.getAmount(), accountEntity.getCurrency());

        accountEntity.setBalance(accountEntity.getBalance().subtract(withdrawRequest.getAmount()));

        accountRepository.save(accountEntity);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(accountEntity)
                .amount(withdrawRequest.getAmount())
                .type(TransactionType.WITHDRAW)
                .description("Withdraw")
                .referenceType(ReferenceType.ACCOUNT)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        transactionRepository.save(transactionEntity);
    }

    @Override
    @Transactional
    public void deposit(PaymentRequest paymentRequest) {

        validationUtil.validateId(paymentRequest.getAccountId());

        AccountEntity accountEntity = accountRepository.findById(paymentRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal deposit = paymentRequest.getAmount();

        if (!accountEntity.getCurrency().equals(Currency.USD)) {
            deposit = convertFromUsd(deposit, accountEntity.getCurrency());
        }


        accountEntity.setBalance(accountEntity.getBalance().add(deposit));

        accountRepository.save(accountEntity);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(accountEntity)
                .amount(paymentRequest.getAmount())
                .type(TransactionType.DEPOSIT)
                .description("Deposit")
                .paymentMethod(paymentRequest.getPaymentMethod())
                .referenceType(ReferenceType.ACCOUNT)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        transactionRepository.save(transactionEntity);
    }


}
