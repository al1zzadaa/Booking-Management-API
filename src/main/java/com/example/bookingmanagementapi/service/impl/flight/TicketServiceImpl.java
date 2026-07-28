package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTicketRequest;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.TicketStatus;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.SeatNotAvailable;
import com.example.bookingmanagementapi.exception.ValidationException;
import com.example.bookingmanagementapi.mapper.TicketMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.TicketService;
import com.example.bookingmanagementapi.service.TransactionService;
import com.example.bookingmanagementapi.service.specifications.TicketSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;
    private final FareBaggageRepository fareBaggageRepository;
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    @Value("${percent10}")
    private Integer percent10;
    @Value("${percent20}")
    private Integer percent20;
    @Value("${percent30}")
    private Integer percent30;
    @Value("${percent50}")
    private Integer percent50;
    @Value("${percent70}")
    private Integer percent70;



    @Override
    @Transactional
    public void book(TicketRequest ticketRequest) {

        validationUtil.validateId(ticketRequest.getUserId());
        validationUtil.validateId(ticketRequest.getFlightId());
        validationUtil.validateId(ticketRequest.getSeatId());
        validationUtil.validateId(ticketRequest.getAccountId());

        UserEntity userEntity = userRepository.findById(ticketRequest.getUserId())
                .orElseThrow();

        AccountEntity accountEntity = accountRepository.findById(ticketRequest.getAccountId())
                .orElseThrow();

        FlightEntity flightEntity = flightRepository.findById(ticketRequest.getFlightId())
                .orElseThrow(() -> new NotFoundException("flight not found"));

        SeatEntity seatEntity = seatRepository.findById(ticketRequest.getSeatId())
                .orElseThrow(() -> new NotFoundException("seat not found"));

        if (!accountEntity.getUser().getId().equals(userEntity.getId())) {
            throw new ValidationException("account not owned by user");
        }

        if (!seatEntity.getFlight().getId().equals(flightEntity.getId())) {
            throw new ValidationException("flight does not have this seat");
        }

        if (!seatEntity.getIsAvailable()) {
            throw new SeatNotAvailable("Seat not available");
        }

        FareBaggageEntity policy = fareBaggageRepository.findByAirlineAndTicketClass(
                        flightEntity.getAirline(),
                        seatEntity.getTicketClass())
                .orElseThrow(() ->
                        new NotFoundException("Fare baggage policy not found"));


        TicketEntity ticket = TicketEntity.builder()
                .user(userEntity)
                .account(accountEntity)
                .flight(flightEntity)
                .seat(seatEntity)
                .price(policy.getPrice().add(flightEntity.getBasePrice()))
                .fareBaggage(policy)
                .status(TicketStatus.RESERVED)
                .build();

        seatEntity.setIsAvailable(false);
        ticketRepository.save(ticket);

        notificationService.sendBookingNotification(userEntity.getId());

    }

    @Transactional
    @Override
    public void payTicket(Long ticketId, PaymentRequest request) {

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(null);

        transactionService.payForTicket(ticket, request);

        ticket.setStatus(TicketStatus.CONFIRMED);

        ticketRepository.save(ticket);

        notificationService.sendTicketPaymentNotification(ticket);
    }

    @Override
    @Transactional
    public void cancel(Long ticketId) {

        validationUtil.validateId(ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("ticket not found"));

        BigDecimal refund = calculateRefund(ticketId);

        transactionService.refund(ticket, refund);

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.getSeat().setIsAvailable(true);

        notificationService.sendCancellationNotification(ticket);
    }



    private @NonNull BigDecimal calculateRefund(Long ticketId) {
        validationUtil.validateId(ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow();

        BigDecimal refund = ticket.getPrice();

        LocalDateTime departure = ticket.getFlight().getDepartureTime();
        LocalDateTime now = LocalDateTime.now();

        if (departure.isBefore(now)) {
            throw new IllegalStateException("Flight has already departed");
        }

        long daysLeft = ChronoUnit.DAYS.between(now, departure);
        BigDecimal res = getBigDecimal(daysLeft, refund);

        return refund.subtract(res);
    }

    private BigDecimal getBigDecimal(long daysLeft, BigDecimal refund) {
        int cancellationFee;

        if (daysLeft >= 30) {
            cancellationFee = percent10;
        } else if (daysLeft >= 15) {
            cancellationFee = percent20;
        } else if (daysLeft >= 7) {
            cancellationFee = percent30;
        } else if (daysLeft >= 3) {
            cancellationFee = percent50;
        } else {
            cancellationFee = percent70;
        }

        return refund.multiply(BigDecimal.valueOf(cancellationFee)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }


    @Override
    public List<TicketResponse> findAll(TicketFilter ticketFilter) {

        var specification = new TicketSpecification(ticketFilter);

        List<TicketEntity> ticketEntities = ticketRepository.findAll(specification);

        return ticketMapper.toListDto(ticketEntities);
    }

    @Override
    public TicketResponse findById(Long id) {

        validationUtil.validateId(id);

        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ticket not found"));

        return ticketMapper.toDto(ticket);
    }

    @Override
    public void updateTicket(Long ticketId, UpdateTicketRequest updateTicketRequest) {

        validationUtil.validateId(ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("ticket not found"));

        ticketMapper.updateTicket(ticketId, updateTicketRequest);

        ticketRepository.save(ticket);
    }

    @Override
    public void deleteTicketById(Long id) {

        validationUtil.validateId(id);

        if (!ticketRepository.existsById(id)) {
            throw new NotFoundException("ticket not found");
        }

        ticketRepository.deleteById(id);
    }
}
