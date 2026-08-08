package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import com.example.bookingmanagementapi.exception.InsufficientBalanceException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.PaymentAlreadyCompletedException;
import com.example.bookingmanagementapi.mapper.TransactionMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.ConvertService;
import com.example.bookingmanagementapi.service.PromoCodeService;
import com.example.bookingmanagementapi.service.TransactionService;
import com.example.bookingmanagementapi.service.UserPromoCodeService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ValidationUtil validationUtil;
    private final AccountRepository accountRepository;
    private final ConvertService convert;
    private final PromoCodeService promoCodeService;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final UserPromoCodeService userPromoCodeService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

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

    private void validateNotAlreadyPaid(Long referenceId, ReferenceType referenceType) {
        if (transactionRepository.existsByReferenceIdAndReferenceTypeAndPaymentStatus(
                referenceId,
                referenceType,
                PaymentStatus.SUCCESS)) {
            throw new PaymentAlreadyCompletedException("Payment is already paid");
        }
    }

    @Transactional
    @Override
    public void payForTicket(TicketEntity ticket, PaymentRequest paymentRequest) {

        validateNotAlreadyPaid(ticket.getId(), ReferenceType.FLIGHT_TICKET);

        AccountEntity account = ticketAndBookingPaymentDuplicate(
                paymentRequest.getAccountId(),
                ticket.getPrice(),
                paymentRequest.getPromoCode()
        );

        BigDecimal finalAmount = promoCodeService.calculateFinalAmount(
                ticket.getPrice(),
                paymentRequest.getPromoCode()
        );

        ticket.setPrice(finalAmount);
        ticketRepository.save(ticket);

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

        applyUserPromoCode(ticket.getUser().getId(), paymentRequest.getPromoCode());
    }


    @Transactional
    public void refundTicket(TicketEntity ticket, BigDecimal amount) {
        AccountEntity account = ticket.getAccount();

        BigDecimal refundAmount =  convert.convert(
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

        BigDecimal refundAmount =  convert.convert(
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

        BigDecimal amountToWithdraw =  convert.convert(
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
            deposit =  convert.convert(
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

        validateNotAlreadyPaid(booking.getId(), ReferenceType.HOTEL_BOOKING);

        AccountEntity account = ticketAndBookingPaymentDuplicate(
                paymentRequest.getAccountId(),
                booking.getTotalPrice(),
                paymentRequest.getPromoCode());

        BigDecimal finalAmount = promoCodeService.calculateFinalAmount(
                booking.getTotalPrice(),
                paymentRequest.getPromoCode()
        );

        booking.setTotalPrice(finalAmount);
        bookingRepository.save(booking);

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

        applyUserPromoCode(booking.getUser().getId(), paymentRequest.getPromoCode());
    }

    private void applyUserPromoCode(Long userId, String promoCode){
        userPromoCodeService.applyPromoCode(
                userId,
                promoCode
        );
    }


    private AccountEntity ticketAndBookingPaymentDuplicate(Long accountId,
                                                           BigDecimal amountInUsd,
                                                           String promoCode) {

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal amountToWithdraw = convert.convert(
                amountInUsd,
                Currency.USD,
                account.getCurrency()
        );

        BigDecimal finalAmount = promoCodeService.calculateFinalAmount(
                amountToWithdraw,
                promoCode
        );

        if (account.getBalance().compareTo(finalAmount) < 0) {
            throw new InsufficientBalanceException("Not enough balance");
        }

        account.setBalance(account.getBalance().subtract(finalAmount));

        promoCodeService.markAsUsed(promoCode);

        return account;
    }

    @Transactional
    @Override
    public void payForSubscription(SubscriptionRequest  subscriptionRequest){

        validateNotAlreadyPaid(subscriptionRequest.getPlanId(), ReferenceType.SUBSCRIPTION);

        SubscriptionPlanEntity subscriptionPlanEntity =
                subscriptionPlanRepository.findByIdAndActive(subscriptionRequest.getPlanId(), true);

        BigDecimal subscriptionAmount = subscriptionPlanEntity.getPrice();

        AccountEntity account = ticketAndBookingPaymentDuplicate(
                subscriptionRequest.getAccountId(),
                subscriptionAmount,
                null);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .amount(subscriptionAmount)
                .account(account)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceType(ReferenceType.SUBSCRIPTION)
                .paymentMethod(subscriptionRequest.getPaymentMethod())
                .type(TransactionType.PAYMENT)
                .description("Payment for subscription")
                .referenceId(subscriptionRequest.getPlanId())
                .build();

        transactionRepository.save(transactionEntity);
    }

}
