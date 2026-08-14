package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.*;
import com.example.bookingmanagementapi.exception.InsufficientBalanceException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.PaymentAlreadyCompletedException;
import com.example.bookingmanagementapi.exception.ValidationException;
import com.example.bookingmanagementapi.mapper.TransactionMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.*;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    private final FlightBookingRepository flightBookingRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final LoyaltyPointService loyaltyPointService;

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
    public void payForTickets(Long flightBookingId, PaymentRequest paymentRequest) {
        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        List<TicketEntity> tickets = flightBooking.getTickets();

        if (tickets == null || tickets.isEmpty()) {
            throw new ValidationException("No tickets to pay");
        }

        BigDecimal totalAmount = tickets.stream()
                .map(TicketEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalAmount = promoCodeService.calculateFinalAmount(
                totalAmount,
                paymentRequest.getPromoCode()
        );

        AccountEntity account = ticketAndBookingPaymentDuplicate(
                paymentRequest.getAccountId(),
                finalAmount,
                null
        );

        Integer points = paymentRequest.getLoyaltyPointsToUse();

        if (points != null) {
            loyaltyPointService.usePoints(
                    account.getId(),
                    points,
                    "Points used for flight ticket payment"
            );
        }
//        AccountEntity account = ticketAndBookingPaymentDuplicate(
//                paymentRequest.getAccountId(),
//                ticket.getPrice(),
//                paymentRequest.getPromoCode()
//        );
//
//        BigDecimal finalAmount = promoCodeService.calculateFinalAmount(
//                ticket.getPrice(),
//                paymentRequest.getPromoCode()
//        );
//
//        ticket.setPrice(finalAmount);
//        ticketRepository.save(ticket);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .amount(finalAmount)
                .account(account)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceType(ReferenceType.FLIGHT_TICKET)
                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
                .type(TransactionType.PAYMENT)
                .description("Payment for ticket")
                .referenceId(flightBookingId)
                .build();

        transactionRepository.save(transactionEntity);

//        ticketRepository.saveAll(tickets);
//        transactionRepository.save(transactionEntity);

        applyUserPromoCode(flightBooking.getUser().getId(), paymentRequest.getPromoCode());
    }


    @Transactional
    public void refundTicket(FlightBookingEntity flightBookingEntity, BigDecimal amount) {
        AccountEntity account = flightBookingEntity.getAccount();

        BigDecimal refundAmount = convert.convert(
                amount,
                Currency.USD,
                account.getCurrency()
        );

        account.setBalance(account.getBalance().add(refundAmount));

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(account)
                .amount(refundAmount)
                .type(TransactionType.REFUND)
                .description("Refund")
                .referenceId(flightBookingEntity.getId())
                .paymentStatus(PaymentStatus.REFUNDED)
                .referenceType(ReferenceType.FLIGHT_TICKET)
                .build();

        transactionRepository.save(transactionEntity);
    }


    @Transactional
    public void refundBooking(BookingEntity booking, BigDecimal amount) {
        AccountEntity account = booking.getAccount();

        BigDecimal refundAmount = convert.convert(
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

        BigDecimal amountToWithdraw = convert.convert(
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
            deposit = convert.convert(
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
                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
                .type(TransactionType.PAYMENT)
                .description("Payment for booking")
                .referenceId(booking.getId())
                .build();

        transactionRepository.save(transactionEntity);

        applyUserPromoCode(booking.getUser().getId(), paymentRequest.getPromoCode());
    }

    private void applyUserPromoCode(Long userId, String promoCode) {
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
    public void payForSubscription(SubscriptionRequest subscriptionRequest, SubscriptionPlanEntity subscriptionPlanEntity) {

        validateNotAlreadyPaid(subscriptionRequest.getPlanId(), ReferenceType.SUBSCRIPTION);

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
                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
                .type(TransactionType.PAYMENT)
                .description("Payment for subscription")
                .referenceId(subscriptionRequest.getPlanId())
                .build();

        transactionRepository.save(transactionEntity);
    }

    @Override
    public void subscriptionRenew(SubscriptionRequest subscriptionRequest,
                                  Long subscriptionId,
                                  SubscriptionPlanEntity subscriptionPlanEntity) {

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
                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
                .type(TransactionType.PAYMENT)
                .description("Payment for subscription renewal")
                .referenceId(subscriptionId)
                .build();

        transactionRepository.save(transactionEntity);
    }


}
