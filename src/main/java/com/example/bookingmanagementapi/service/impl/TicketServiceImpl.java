package com.example.bookingmanagementapi.service.impl;

import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PassengerRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.response.FlightBookingResponse;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.Flights;
import com.example.bookingmanagementapi.enums.TicketStatus;
import com.example.bookingmanagementapi.event.BookingPaymentEvent;
import com.example.bookingmanagementapi.exception.*;
import com.example.bookingmanagementapi.mapper.FlightBookingMapper;
import com.example.bookingmanagementapi.mapper.TicketMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.*;
import com.example.bookingmanagementapi.service.specifications.TicketSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;
    private final FareBaggageRepository fareBaggageRepository;
    private final ValidationUtil validationUtil;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final LoyaltyPointService loyaltyPointService;
    private final ApplicationEventPublisher eventPublisher;
    private final FlightBookingRepository flightBookingRepository;
    private final AccountService accountService;
    private final FlightBookingMapper flightBookingMapper;
    private final CalculationService calculationService;

    @Override
    @Transactional
    public void book(TicketRequest ticketRequest,
                     String username) {

        validationUtil.validateRequestSeats(ticketRequest);
        validationUtil.validatePassengerAges(ticketRequest);

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        AccountEntity accountEntity = accountRepository.findById(ticketRequest.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        userService.validateUserCanBook(user.getId());
        accountService.validateAccountCanBook(ticketRequest.getAccountId());


        FlightEntity flightEntity = flightRepository.findById(ticketRequest.getFlightId())
                .orElseThrow(() -> new NotFoundException("flight not found"));

        if (flightEntity.getStatus() != Flights.SCHEDULED) {
            throw new FlightException("Flight is not available for booking");
        }

        if (flightEntity.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new FlightException("Flight has already departed");
        }

        if (!accountEntity.getUser().getId().equals(user.getId())) {
            throw new ValidationException("account not owned by user");
        }

        FlightBookingEntity flightBooking = FlightBookingEntity.builder()
                .user(user)
                .account(accountEntity)
                .flight(flightEntity)
                .status(TicketStatus.RESERVED)
                .paymentDeadline(LocalDateTime.now().plusMinutes(15))
                .build();

        List<TicketEntity> tickets = new ArrayList<>();

        BigDecimal totalBookingPrice = BigDecimal.ZERO;


        for (PassengerRequest passenger : ticketRequest.getPassengers()) {

            System.out.println("Seat ID: " + passenger.getSeatId());

            SeatEntity seatEntity = seatRepository.findByIdForUpdate(passenger.getSeatId())
                    .orElseThrow(() -> new NotFoundException("Seat not found"));

            if (!seatEntity.getFlight().getId().equals(flightEntity.getId())) {
                throw new ValidationException("Flight does not have this seat");
            }

            if (!seatEntity.getIsAvailable()) {
                throw new SeatNotAvailable("Seat " + seatEntity.getId() + " is not available");
            }

            FareBaggageEntity policy = fareBaggageRepository
                    .findByAirlineAndTicketClass(
                            flightEntity.getAirline(),
                            seatEntity.getTicketClass())
                    .orElseThrow(() ->
                            new NotFoundException("Fare baggage policy not found"));

            totalBookingPrice = calculationService.calculateFlightTotalPrice(ticketRequest, flightEntity);

            TicketEntity ticket = TicketEntity.builder()
                    .seat(seatEntity)
                    .price(totalBookingPrice)
                    .fareBaggage(policy)
                    .status(TicketStatus.RESERVED)
                    .flightBooking(flightBooking)
                    .build();

            tickets.add(ticket);

            seatEntity.setIsAvailable(false);
        }

        flightBooking.setTickets(tickets);
        flightBooking.setTotalPrice(totalBookingPrice);

        flightBookingRepository.save(flightBooking);

        log.info(
                "Flight booking created: bookingId={}, userId={}, flightId={}, accountId={}, passengers={}, totalPrice={}",
                flightBooking.getId(),
                user.getId(),
                flightEntity.getId(),
                accountEntity.getId(),
                tickets.size(),
                totalBookingPrice
        );

        notificationService.sendBookingNotification(
                user.getId()
        );

    }



    @Transactional
    @Override
    public void payTicket(String username,
                          Long flightBookingId,
                          PaymentRequest request) {

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        if (!flightBooking.getUser().getId().equals(user.getId())) {
            throw new ValidationException(
                    "Flight booking does not belong to user"
            );
        }

        List<TicketEntity> tickets = flightBooking.getTickets();

        transactionService.payForTickets(
                flightBookingId,
                request
        );

        for (TicketEntity ticket : tickets) {

            validationUtil.validateTicketCanBePaid(ticket);

            ticket.setStatus(TicketStatus.CONFIRMED);

            loyaltyPointService.earnPoints(
                    flightBooking.getUser().getId(),
                    ticket.getPrice(),
                    "Points earned from ticket payment"
            );
        }

        flightBooking.setStatus(TicketStatus.CONFIRMED);

        log.info("Payment for flightBooking with id: '{}'", flightBookingId);

        eventPublisher.publishEvent(
                new BookingPaymentEvent(user.getId())
        );
    }

    @Override
    @Transactional
    public void cancel(String username, Long flightBookingId) {

        UserEntity userEntity = userRepository
                .findByEmail(username).orElseThrow(() -> new NotFoundException("User not found"));


        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        if (!flightBooking.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("Account does not belong to user");
        }

        List<TicketEntity> tickets = flightBooking.getTickets();


        for (TicketEntity ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.CONFIRMED) {
                throw new ValidationException("ticket not paid");
            }
        }


        BigDecimal refund = calculationService.calculateTicketRefund(flightBookingId);

        transactionService.refundTicket(flightBooking, refund);

        flightBooking.setStatus(TicketStatus.CANCELLED);

        for (TicketEntity ticket : tickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.getSeat().setIsAvailable(true);
        }

        log.info("Flight booking has been cancelled with id '{}'", flightBookingId);

        notificationService.sendTicketCancellationNotification(flightBooking.getUser().getId());
    }

    @Override
    public List<TicketResponse> findAll(TicketFilter ticketFilter) {

        var specification = new TicketSpecification(ticketFilter);

        List<TicketEntity> ticketEntities = ticketRepository.findAll(specification);

        return ticketMapper.toListDto(ticketEntities);
    }

    @Override
    public TicketResponse findById(Long id) {

        TicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ticket not found"));

        return ticketMapper.toDto(ticket);
    }

    @Override
    public void deleteTicketById(Long id) {

        if (!ticketRepository.existsById(id)) {
            throw new NotFoundException("ticket not found");
        }

        ticketRepository.deleteById(id);
    }

    @Override
    public Page<@NonNull FlightBookingResponse> getUserTickets(String username, Pageable pageable) {

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Page<@NonNull FlightBookingEntity> flightBookingEntities = flightBookingRepository.findAllByUser(user, pageable);

        return flightBookingEntities.map(flightBookingMapper::toDto);
    }
}
