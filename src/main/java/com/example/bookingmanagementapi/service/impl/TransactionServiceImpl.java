package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.request.*;
import com.example.bookingmanagementapi.dto.response.PaymentResult;
import com.example.bookingmanagementapi.dto.response.TransactionResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.Currency;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import com.example.bookingmanagementapi.exception.*;
import com.example.bookingmanagementapi.mapper.TransactionMapper;
import com.example.bookingmanagementapi.repository.AccountRepository;
import com.example.bookingmanagementapi.repository.BookingRepository;
import com.example.bookingmanagementapi.repository.FlightBookingRepository;
import com.example.bookingmanagementapi.repository.TransactionRepository;
import com.example.bookingmanagementapi.service.*;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
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
    private final UserPromoCodeService userPromoCodeService;
    private final FlightBookingRepository flightBookingRepository;
    private final LoyaltyPointService loyaltyPointService;

    private void createPayment(
            PaymentResult paymentResult,
            ReferenceType referenceType,
            Long referenceId,
            String description
    ) {
        TransactionEntity transaction = TransactionEntity.builder()
                .amount(paymentResult.finalAmount())
                .amountInUsd(paymentResult.finalAmountInUsd())
                .currency(paymentResult.account().getCurrency())
                .account(paymentResult.account())
                .paymentStatus(PaymentStatus.PAID)
                .referenceType(referenceType)
                .type(TransactionType.PAYMENT)
                .description(description)
                .referenceId(referenceId)
                .build();

        transactionRepository.save(transaction);
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

        log.info("Transaction with id: '{}' has been deleted", transactionId);
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
//        if (transactionRepository.existsByReferenceIdAndReferenceTypeAndPaymentStatus(
//                referenceId,
//                referenceType,
//                PaymentStatus.PAID)) {
//            throw new PaymentAlreadyCompletedException("Payment is already paid");
//        }
        if (transactionRepository
                .existsByReferenceIdAndReferenceTypeAndType(
                        referenceId,
                        referenceType,
                        TransactionType.PAYMENT))
        {
            throw new PaymentException("Already paid");
        }
    }

    private void applyUserPromoCode(Long userId, String promoCode) {
        userPromoCodeService.applyPromoCode(
                userId,
                promoCode
        );
    }

    @Transactional
    @Override
    public void payForTickets(Long flightBookingId, PaymentRequest paymentRequest) {
        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        if (!flightBooking.getAccount().getId().equals(paymentRequest.getAccountId())) {
            throw new AccessDeniedException("This booking does not belong to this account");
        }

        validateNotAlreadyPaid(
                flightBookingId,
                ReferenceType.FLIGHT_TICKET
        );

        List<TicketEntity> tickets = flightBooking.getTickets();

        if (tickets == null || tickets.isEmpty()) {
            throw new ValidationException("No tickets to pay");
        }

        BigDecimal totalAmount = tickets.stream()
                .map(TicketEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer points = paymentRequest.getLoyaltyPointsToUse();

        BigDecimal loyaltyPointsValue =
                loyaltyPointService.pointValue(points);

        PaymentResult paymentResult = processAccountPayment(
                paymentRequest.getAccountId(),
                totalAmount,
                paymentRequest.getPromoCode(),
                loyaltyPointsValue
        );

        if (points != null && points > 0) {
            loyaltyPointService.usePoints(
                    paymentResult.account().getId(),
                    points,
                    "Points used for flight ticket payment"
            );
        }

        flightBooking.setTotalPrice(paymentResult.finalAmountInUsd());
        flightBookingRepository.save(flightBooking);

        createPayment(
                paymentResult,
                ReferenceType.FLIGHT_TICKET,
                flightBookingId,
                "Payment for ticket"
        );
//        TransactionEntity transactionEntity = TransactionEntity.builder()
//                .amount(paymentResult.finalAmount())
//                .amountInUsd(paymentResult.finalAmountInUsd())
//                .currency(paymentResult.account().getCurrency())
//                .account(paymentResult.account())
//                .paymentStatus(PaymentStatus.PAID)
//                .referenceType(ReferenceType.FLIGHT_TICKET)
//                .type(TransactionType.PAYMENT)
//                .description("Payment for ticket")
//                .referenceId(flightBookingId)
//                .build();
//
//        transactionRepository.save(transactionEntity);


//    / /        ticketRepository.saveAll(tickets);
//    / /        transactionRepository.save(transactionEntity);

        log.info("Payment for ticket with id: '{}'", flightBookingId);

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
                .amountInUsd(amount)
                .currency(account.getCurrency())
                .type(TransactionType.REFUND)
                .description("Refund")
                .referenceId(flightBookingEntity.getId())
                .paymentStatus(PaymentStatus.REFUNDED)
                .referenceType(ReferenceType.FLIGHT_TICKET)
                .build();

        transactionRepository.save(transactionEntity);

        log.info("Refund for ticket with id: '{}'", flightBookingEntity.getId());
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
                .amountInUsd(amount)
                .currency(account.getCurrency())
                .type(TransactionType.REFUND)
                .description("Refund")
                .referenceId(booking.getId())
                .paymentStatus(PaymentStatus.REFUNDED)
                .referenceType(ReferenceType.HOTEL_BOOKING)
                .build();

        transactionRepository.save(transactionEntity);

        log.info("Refund for hotel booking with id: '{}'", booking.getId());
    }

    @Override
    @Transactional()
    public void withdraw(WithdrawRequest withdrawRequest) {

        validationUtil.validateId(withdrawRequest.getAccountId());

        AccountEntity accountEntity = accountRepository.findById(withdrawRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal balance = accountEntity.getBalance();

        BigDecimal amountToWithdraw = convert.convert(
                withdrawRequest.getAmount(),
                withdrawRequest.getCurrency(),
                accountEntity.getCurrency()
        );

        if (balance.compareTo(amountToWithdraw) < 0) {
            throw new InsufficientBalanceException("Not enough balance to  withdraw");
        }

        accountEntity.setBalance(accountEntity.getBalance().subtract(amountToWithdraw));

        accountRepository.save(accountEntity);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(accountEntity)
                .amount(withdrawRequest.getAmount())
                .type(TransactionType.WITHDRAW)
                .description("Withdraw")
                .referenceType(ReferenceType.ACCOUNT)
                .paymentStatus(PaymentStatus.PAID)
                .referenceId(accountEntity.getId())
                .build();

        transactionRepository.save(transactionEntity);

        log.info("Withdraw for account with id: '{}'", accountEntity.getId());
    }

    @Override
    @Transactional
    public void deposit(DepositRequest depositRequest) {

        validationUtil.validateId(depositRequest.getAccountId());

        AccountEntity accountEntity = accountRepository.findById(depositRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        BigDecimal deposit = depositRequest.getAmount();

//        if (!depositRequest.getCurrency().equals(accountEntity.getCurrency())) {
//            deposit = convert.convert(
//                    deposit,
//                    depositRequest.getCurrency(),
//                    accountEntity.getCurrency()
//            );
//        }

        BigDecimal usdAmount = convert.convert(
                depositRequest.getAmount(),
                depositRequest.getCurrency(),
                Currency.USD);

        BigDecimal convertedAmount = convert.convert(
                depositRequest.getAmount(),
                depositRequest.getCurrency(),
                accountEntity.getCurrency());

        accountEntity.setBalance(accountEntity.getBalance().add(convertedAmount));

        accountRepository.save(accountEntity);

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .account(accountEntity)
                .currency(accountEntity.getCurrency())
                .amountInUsd(usdAmount)
                .amount(convertedAmount)
                .type(TransactionType.DEPOSIT)
                .description("Deposit")
                .referenceType(ReferenceType.ACCOUNT)
                .paymentStatus(PaymentStatus.PAID)
                .referenceId(accountEntity.getId())
                .build();

        transactionRepository.save(transactionEntity);

        log.info("Deposit for account with id: '{}'", accountEntity.getId());
    }

    //    @Transactional
//    @Override
//    public void payForBooking(BookingEntity booking, PaymentRequest paymentRequest) {
//
//        validateNotAlreadyPaid(booking.getId(), ReferenceType.HOTEL_BOOKING);
//
//        PaymentResult result = processAccountPayment(
//                paymentRequest.getAccountId(),
//                booking.getTotalPrice(),
//                paymentRequest.getPromoCode()
//        );
//
//        booking.setTotalPrice(result.finalAmount());
//        bookingRepository.save(booking);
//
//        TransactionEntity transactionEntity = TransactionEntity.builder()
//                .amount(result.finalAmount())
//                .account(result.account())
//                .paymentStatus(PaymentStatus.SUCCESS)
//                .referenceType(ReferenceType.HOTEL_BOOKING)
//                .paymentMethod(PaymentMethods.ACCOUNT_BALANCE)
//                .type(TransactionType.PAYMENT)
//                .description("Payment for booking")
//                .referenceId(booking.getId())
//                .build();
//
//        transactionRepository.save(transactionEntity);
//
//        applyUserPromoCode(booking.getUser().getId(), paymentRequest.getPromoCode());
//    }
    @Transactional
    @Override
    public void payForBooking(
            BookingEntity booking,
            PaymentRequest paymentRequest
    ) {

        if (!booking.getAccount().getId().equals(paymentRequest.getAccountId())) {
            throw new AccessDeniedException("This booking does not belong to this account");
        }

        validateNotAlreadyPaid(
                booking.getId(),
                ReferenceType.HOTEL_BOOKING
        );

        Integer points = paymentRequest.getLoyaltyPointsToUse();

        BigDecimal loyaltyPointsValue =
                loyaltyPointService.pointValue(points);

        PaymentResult paymentResult = processAccountPayment(
                paymentRequest.getAccountId(),
                booking.getTotalPrice(),
                paymentRequest.getPromoCode(),
                loyaltyPointsValue
        );

        if (points != null && points > 0) {
            loyaltyPointService.usePoints(
                    paymentResult.account().getId(),
                    points,
                    "Points used for hotel booking payment"
            );
        }

        booking.setTotalPrice(paymentResult.finalAmountInUsd());
        bookingRepository.save(booking);

        createPayment(
                paymentResult,
                ReferenceType.HOTEL_BOOKING,
                booking.getId(),
                "Payment for booking"
        );

//        TransactionEntity transactionEntity = TransactionEntity.builder()
//                .amount(paymentResult.finalAmount())
//                .amountInUsd(paymentResult.finalAmountInUsd())
//                .currency(paymentResult.account().getCurrency())
//                .account(paymentResult.account())
//                .paymentStatus(PaymentStatus.PAID)
//                .referenceType(ReferenceType.HOTEL_BOOKING)
//                .type(TransactionType.PAYMENT)
//                .description("Payment for booking")
//                .referenceId(booking.getId())
//                .build();
//
//        transactionRepository.save(transactionEntity);


        log.info("Payment for booking with id: '{}'", booking.getId());

        applyUserPromoCode(
                booking.getUser().getId(),
                paymentRequest.getPromoCode()
        );
    }


    private PaymentResult processAccountPayment(
            Long accountId,
            BigDecimal amountInUsd,
            String promoCode,
            BigDecimal loyaltyPointsValueInUsd
    ) {

        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        // 1. Apply promo in USD
        BigDecimal finalAmountInUsd =
                promoCodeService.calculateFinalAmount(
                        amountInUsd,
                        promoCode
                );

        // 2. Apply loyalty points in USD
        if (loyaltyPointsValueInUsd != null
                && loyaltyPointsValueInUsd.compareTo(BigDecimal.ZERO) > 0) {

            if (loyaltyPointsValueInUsd.compareTo(finalAmountInUsd) > 0) {
                throw new ValidationException(
                        "Loyalty points cannot exceed payment amount"
                );
            }

            finalAmountInUsd = finalAmountInUsd.subtract(
                    loyaltyPointsValueInUsd
            );
        }

        // 3. Convert FINAL USD amount to account currency
        BigDecimal finalAmount = convert.convert(
                finalAmountInUsd,
                Currency.USD,
                account.getCurrency()
        );

        // 4. Check balance
        if (account.getBalance().compareTo(finalAmount) < 0) {
            throw new InsufficientBalanceException("Not enough balance");
        }

        // 5. Withdraw
        account.setBalance(
                account.getBalance().subtract(finalAmount)
        );

        // 6. Mark promo as used
        promoCodeService.markAsUsed(promoCode);

        // finalAmount       = account currency
        // finalAmountInUsd   = USD
        return new PaymentResult(
                account,
                finalAmountInUsd,
                finalAmount
        );
    }

    @Transactional
    @Override
    public void processSubscriptionPayment(SubscriptionRequest subscriptionRequest,
                                           Long subscriptionId,
                                           SubscriptionPlanEntity subscriptionPlanEntity,
                                           String description) {

        BigDecimal subscriptionAmount = subscriptionPlanEntity.getPrice();

        PaymentResult paymentResult = processAccountPayment(
                subscriptionRequest.getAccountId(),
                subscriptionAmount,
                null,
                null);

        createPayment(
                paymentResult,
                ReferenceType.SUBSCRIPTION,
                subscriptionId,
                description
        );

        log.info("Subscription payment for user with id: '{}'", subscriptionId);

//        TransactionEntity transactionEntity = TransactionEntity.builder()
//                .amount(paymentResult.finalAmount())
//                .amountInUsd(paymentResult.finalAmountInUsd())
//                .account(paymentResult.account())
//                .currency(paymentResult.account().getCurrency())
//                .paymentStatus(PaymentStatus.PAID)
//                .referenceType(ReferenceType.SUBSCRIPTION)
//                .type(TransactionType.PAYMENT)
//                .description(description)
//                .referenceId(subscriptionId)
//                .build();
//
//        transactionRepository.save(transactionEntity);
    }

//    @Transactional
//    @Override
//    public void subscriptionRenew(SubscriptionRequest subscriptionRequest,
//                                  Long subscriptionId,
//                                  SubscriptionPlanEntity subscriptionPlanEntity,
//                                  String description) {
//
//        BigDecimal subscriptionAmount = subscriptionPlanEntity.getPrice();
//
//        PaymentResult paymentResult = processAccountPayment(
//                subscriptionRequest.getAccountId(),
//                subscriptionAmount,
//                null,
//                null);
//
//        createPayment(
//                paymentResult,
//                ReferenceType.SUBSCRIPTION,
//                subscriptionId,
//                description
//        );
//
//        TransactionEntity transactionEntity = TransactionEntity.builder()
//                .amount(paymentResult.finalAmount())
//                .amountInUsd(paymentResult.finalAmountInUsd())
//                .currency(paymentResult.account().getCurrency())
//                .account(paymentResult.account())
//                .paymentStatus(PaymentStatus.PAID)
//                .referenceType(ReferenceType.SUBSCRIPTION)
//                .type(TransactionType.PAYMENT)
//                .description(description)
//                .referenceId(subscriptionId)
//                .build();
//
//        transactionRepository.save(transactionEntity);
//    }


}
