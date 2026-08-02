package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.BookingEntity;
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
    private static final BigDecimal USD_TO_AZN = BigDecimal.valueOf(1.7);
    private static final BigDecimal USD_TO_TRY = BigDecimal.valueOf(43);
    private static final BigDecimal USD_TO_RUB = BigDecimal.valueOf(80);
    private static final BigDecimal EUR_TO_USD = BigDecimal.valueOf(1.14);

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

        AccountEntity account = ticketAndBookingPaymentDuplicate(
                paymentRequest.getAccountId(),
                ticket.getPrice()
        );

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .amount(ticket.getPrice())
                .account(account)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceType(ReferenceType.FLIGHT_TICKET)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .type(TransactionType.PAYMENT)
                .description("Payment for ticket")
                .referenceId(ticket.getId())
                .build();

        transactionRepository.save(transactionEntity);
    }

//    private BigDecimal convertToUsd(BigDecimal amount, Currency currency) {
//        return switch (currency) {
//            case USD -> amount;
//            case AZN -> amount.divide(BigDecimal.valueOf(1.7), 2, RoundingMode.HALF_UP);
//            case EUR -> amount.multiply(BigDecimal.valueOf(1.14)).setScale(2, RoundingMode.HALF_UP);
//            default -> BigDecimal.ZERO;
//        };
//    }
//
//    private BigDecimal convertFromUsd(BigDecimal amount, Currency currency) {
//        return switch (currency) {
//            case USD -> amount;
//            case AZN -> amount.multiply(BigDecimal.valueOf(1.7)).setScale(2, RoundingMode.HALF_UP);
//            case EUR -> amount.divide(BigDecimal.valueOf(1.14), 2, RoundingMode.HALF_UP);
//            default -> BigDecimal.ZERO;
//        };
//    }

    @Transactional
    public void refundTicket(TicketEntity ticket, BigDecimal amount) {
        AccountEntity account = ticket.getAccount();

        BigDecimal refundAmount = convert(
                amount,
                Currency.USD,
                account.getCurrency()
        );

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


    @Transactional
    public void refundBooking(BookingEntity booking, BigDecimal amount) {
        AccountEntity account = booking.getAccount();

        BigDecimal refundAmount = convert(
                amount,
                Currency.USD,
                account.getCurrency()
        );

        account.setBalance(account.getBalance().add(refundAmount));

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(booking.getAccount())
                .amount(refundAmount)
                .type(TransactionType.REFUND)
                .description("Refund")
                .referenceId(booking.getId())
                .paymentStatus(PaymentStatus.REFUNDED)
                .referenceType(ReferenceType.HOTEL_BOOKING)
                .build();

        transactionRepository.save(transactionEntity);
    }

    @Override
    @Transactional()
    public void withdraw(WithdrawRequest withdrawRequest) {

        validationUtil.validateId(withdrawRequest.getAccountId());

        AccountEntity accountEntity = accountRepository.findById(withdrawRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal balance = accountEntity.getBalance();

        if (balance.compareTo(withdrawRequest.getAmount()) < 0) {
            throw new InsufficientBalanceException("Not enough balance to  withdraw");
        }

        BigDecimal amountToWithdraw = convert(
                withdrawRequest.getAmount(),
                withdrawRequest.getCurrency(),
                accountEntity.getCurrency()
        );

        accountEntity.setBalance(accountEntity.getBalance().subtract(amountToWithdraw));

        accountRepository.save(accountEntity);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(accountEntity)
                .amount(withdrawRequest.getAmount())
                .type(TransactionType.WITHDRAW)
                .description("Withdraw")
                .referenceType(ReferenceType.ACCOUNT)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(accountEntity.getId())
                .build();

        transactionRepository.save(transactionEntity);
    }

    @Override
    @Transactional
    public void deposit(DepositRequest depositRequest) {

        validationUtil.validateId(depositRequest.getAccountId());

        AccountEntity accountEntity = accountRepository.findById(depositRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal deposit = depositRequest.getAmount();

        if (!depositRequest.getCurrency().equals(accountEntity.getCurrency())) {
            deposit = convert(
                    deposit,
                    depositRequest.getCurrency(),
                    accountEntity.getCurrency()
            );
        }

        accountEntity.setBalance(accountEntity.getBalance().add(deposit));

        accountRepository.save(accountEntity);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(accountEntity)
                .amount(depositRequest.getAmount())
                .type(TransactionType.DEPOSIT)
                .description("Deposit")
                .paymentMethod(depositRequest.getPaymentMethod())
                .referenceType(ReferenceType.ACCOUNT)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(accountEntity.getId())
                .build();

        transactionRepository.save(transactionEntity);
    }

    @Transactional
    @Override
    public void payForBooking(BookingEntity booking, PaymentRequest paymentRequest) {

        AccountEntity account = ticketAndBookingPaymentDuplicate(
                paymentRequest.getAccountId(),
                booking.getTotalPrice());

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .amount(booking.getTotalPrice())
                .account(account)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceType(ReferenceType.HOTEL_BOOKING)
                .paymentMethod(paymentRequest.getPaymentMethod())
                .type(TransactionType.PAYMENT)
                .description("Payment for booking")
                .referenceId(booking.getId())
                .build();

        transactionRepository.save(transactionEntity);
    }


    private AccountEntity ticketAndBookingPaymentDuplicate(Long accountId, BigDecimal amountInUsd) {

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal amountToWithdraw = convert(
                amountInUsd,
                Currency.USD,
                account.getCurrency()
        );


        if (account.getBalance().compareTo(amountToWithdraw) < 0) {
            throw new InsufficientBalanceException("Not enough balance");
        }

        account.setBalance(
                account.getBalance().subtract(amountToWithdraw)
        );

        return account;
    }

    private BigDecimal convert(BigDecimal amount, Currency from, Currency to) {

        if (from == to) {
            return amount;
        }

        // Convert source currency to USD
        BigDecimal amountInUsd = switch (from) {
            case USD -> amount;

            case AZN -> amount.divide(
                    USD_TO_AZN,
                    2,
                    RoundingMode.HALF_UP
            );

            case EUR -> amount.multiply(
                    EUR_TO_USD
            ).setScale(2, RoundingMode.HALF_UP);

            case TR -> amount.divide(
                    USD_TO_TRY,
                    2,
                    RoundingMode.HALF_UP
            );

            case RUB -> amount.divide(
                    USD_TO_RUB,
                    2,
                    RoundingMode.HALF_UP
            );
        };


        // Convert USD to target currency
        return switch (to) {
            case USD -> amountInUsd;

            case AZN -> amountInUsd.multiply(
                    USD_TO_AZN
            ).setScale(2, RoundingMode.HALF_UP);

            case EUR -> amountInUsd.divide(
                    EUR_TO_USD,
                    2,
                    RoundingMode.HALF_UP
            );

            case TR -> amountInUsd.multiply(
                    USD_TO_TRY
            ).setScale(2, RoundingMode.HALF_UP);

            case RUB -> amountInUsd.multiply(
                    USD_TO_RUB
            ).setScale(2, RoundingMode.HALF_UP);
        };
    }
}
