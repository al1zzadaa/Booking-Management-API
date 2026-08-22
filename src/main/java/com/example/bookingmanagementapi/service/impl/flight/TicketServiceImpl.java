package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.TicketFilter;
import com.example.bookingmanagementapi.dto.request.PassengerRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.TicketRequest;
import com.example.bookingmanagementapi.dto.request.UpdateTicketRequest;
import com.example.bookingmanagementapi.dto.response.flight.TicketResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.TicketStatus;
import com.example.bookingmanagementapi.event.BookingPaymentEvent;
import com.example.bookingmanagementapi.exception.AccessDeniedException;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.exception.SeatNotAvailable;
import com.example.bookingmanagementapi.exception.ValidationException;
import com.example.bookingmanagementapi.mapper.TicketMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.*;
import com.example.bookingmanagementapi.service.specifications.TicketSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final UserService userService;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final TicketAndBookingLogics ticketAndBookingLogics;
    private final LoyaltyPointService loyaltyPointService;
    private final ApplicationEventPublisher eventPublisher;
    private final FlightBookingRepository flightBookingRepository;
    private final AccountService accountService;
    @Value("${booking.children.young-max-age}")
    private Integer youngChildMaxAge;
    @Value("${booking.children.teen-max-age}")
    private Integer teenChildMaxAge;
    @Value("${booking.children.teen-discount-percent}")
    private BigDecimal teenChildDiscountPercent;

    @Override
    @Transactional
    public void book(
            TicketRequest ticketRequest,
            String username) {

        validateRequestSeats(ticketRequest);

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userService.validateUserCanBook(user.getId());
        accountService.validateAccountCanBook(ticketRequest.getAccountId());


        AccountEntity accountEntity = accountRepository.findById(ticketRequest.getAccountId())
                .orElseThrow();

        FlightEntity flightEntity = flightRepository.findById(ticketRequest.getFlightId())
                .orElseThrow(() -> new NotFoundException("flight not found"));

        if (!accountEntity.getUser().getId().equals(user.getId())) {
            throw new ValidationException("account not owned by user");
        }

        System.out.println("Passenger count: " +
                ticketRequest.getPassengers().size());


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

            validationUtil.validateId(passenger.getSeatId());

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

            totalBookingPrice = calculateTotalPrice(ticketRequest, flightEntity);

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

        notificationService.sendBookingNotification(
                user.getId()
        );

    }

    private void validateRequestSeats(TicketRequest ticketRequest) {
        Set<Long> seatIds = ticketRequest.getPassengers()
                .stream()
                .map(PassengerRequest::getSeatId)
                .collect(Collectors.toSet());

        if (seatIds.size() != ticketRequest.getPassengers().size()) {
            throw new ValidationException("Duplicate seat selected");
        }
    }

    private BigDecimal calculatePassengerPrice(
            PassengerRequest passenger,
            FlightEntity flight,
            SeatEntity seat,
            FareBaggageEntity baggagePolicy
    ) {

        BigDecimal baseFare = calculateBaseFare(
                passenger.getAge()
        );

        BigDecimal seatPrice = calculateSeatPrice(seat);

        BigDecimal baggagePrice = calculateBaggagePrice(
                baggagePolicy
        );

        return baseFare
                .add(seatPrice)
                .add(baggagePrice);
    }

    private BigDecimal calculateBaseFare(
            Integer age
    ) {

        BigDecimal basePrice = BigDecimal.ZERO;

        if (age <= youngChildMaxAge) {
            return BigDecimal.ZERO;
        }

        if (age <= teenChildMaxAge) {
            BigDecimal discountPercent = teenChildDiscountPercent;

            return basePrice
                    .multiply(
                            BigDecimal.valueOf(100)
                                    .subtract(discountPercent)
                    )
                    .divide(
                            BigDecimal.valueOf(100),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return basePrice;
    }

    private BigDecimal calculateTotalPrice(
            TicketRequest request,
            FlightEntity flight
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (PassengerRequest passenger : request.getPassengers()) {

            SeatEntity seat = seatRepository.findById(passenger.getSeatId())
                    .orElseThrow(() ->
                            new NotFoundException("Seat not found"));

            FareBaggageEntity policy = fareBaggageRepository
                    .findByAirlineAndTicketClass(
                            flight.getAirline(),
                            seat.getTicketClass()
                    )
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Fare baggage policy not found"
                            ));

            BigDecimal passengerPrice = calculatePassengerPrice(
                    passenger,
                    flight,
                    seat,
                    policy
            );

            total = total.add(passengerPrice);
        }

        return total;
    }

    private BigDecimal calculateSeatPrice(SeatEntity seat) {
        return seat.getPrice();
    }

    private BigDecimal calculateBaggagePrice(
            FareBaggageEntity baggagePolicy
    ) {
        return baggagePolicy.getPrice();
    }

//    private void method(TicketRequest ticketRequest, FlightEntity flightEntity) {
//
//    }

//    private BigDecimal calculatePassengerPrice(
//            PassengerRequest passenger,
//            FlightEntity flight,
//            SeatEntity seat,
//            FareBaggageEntity baggagePolicy
//    ) {
//
//        BigDecimal baseFare = baggagePolicy.getPrice();
//
//        BigDecimal seatPrice = calculateSeatPrice(seat);
//
//        BigDecimal baggagePrice = calculateBaggagePrice(
//                baggagePolicy
//        );
//
//        return baseFare
//                .add(seatPrice)
//                .add(baggagePrice);
//    }
//
//    private BigDecimal calculateBaseFare(
//            Integer age,
//            FlightEntity flight
//    ) {
//
//        BigDecimal basePrice = flight.getPrice();
//
//        if (age <= 2) {
//            return BigDecimal.ZERO;
//        }
//
//        if (age <= 11) {
//            BigDecimal childDiscount = BigDecimal.valueOf(30);
//
//            return basePrice
//                    .multiply(
//                            BigDecimal.valueOf(100)
//                                    .subtract(childDiscount)
//                    )
//                    .divide(
//                            BigDecimal.valueOf(100),
//                            2,
//                            RoundingMode.HALF_UP
//                    );
//        }
//
//        return basePrice;
//    }

    @Transactional
    @Override
    public void payTicket(String username,
                          Long flightBookingId,
                          PaymentRequest request) {

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

//        TicketEntity ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not found"));
//
//        FlightEntity flight = flightRepository.findById(ticket.getFlight().getId()).orElseThrow(() -> new NotFoundException("Flight not found"));

//        List<TicketEntity> tickets = ticketRepository.findAllById(ticketIds);
//
//        if (tickets.isEmpty()) {
//            throw new NotFoundException("No reserved tickets found");
//        }
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

            ticket.setStatus(TicketStatus.CONFIRMED);

            loyaltyPointService.earnPoints(
                    flightBooking.getUser().getId(),
                    ticket.getPrice(),
                    "Points earned from ticket payment"
            );
        }

        flightBooking.setStatus(TicketStatus.CONFIRMED);

//        TicketEntity ticket = ticketRepository.findById(ticketId)
//                .orElseThrow(null);
//
//        transactionService.payForTicket(ticket, request);
//
//        ticket.setStatus(TicketStatus.CONFIRMED);
////
//        ticketRepository.saveAll(tickets);
//        flightBookingRepository.save(flightBooking);
//        loyaltyPointService.earnPoints(
//                ticket.getUser().getId(),
//                ticket.getPrice(),
//                "Points earned from ticket payment");

//        notificationService.sendTicketPaymentNotification(ticket);

        eventPublisher.publishEvent(
                new BookingPaymentEvent(user.getId())
        );
    }

    @Override
    @Transactional
    public void cancel(String username, Long flightBookingId) {

        validationUtil.validateId(flightBookingId);

        UserEntity userEntity = userRepository
                .findByEmail(username).orElseThrow(null);


        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        if (!flightBooking.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("Account does not belong to user");
        }

        List<TicketEntity> tickets = flightBooking.getTickets();

//        TicketEntity ticket = ticketRepository.findById(ticketId)
//                .orElseThrow(() -> new NotFoundException("ticket not found"));

        for (TicketEntity ticket : tickets) {
            if (ticket.getStatus() != TicketStatus.CONFIRMED) {
                throw new ValidationException("ticket not paid");
            }
        }


        BigDecimal refund = calculateTicketRefund(flightBookingId);

        transactionService.refundTicket(flightBooking, refund);

        flightBooking.setStatus(TicketStatus.CANCELLED);

        for (TicketEntity ticket : tickets) {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticket.getSeat().setIsAvailable(true);
        }


        notificationService.sendTicketCancellationNotification(flightBooking.getUser().getId());
    }


    private BigDecimal calculateTicketRefund(Long flightBookingId) {
        validationUtil.validateId(flightBookingId);

        FlightBookingEntity flightBooking =
                flightBookingRepository.findById(flightBookingId)
                        .orElseThrow(() ->
                                new NotFoundException("Flight booking not found"));

        List<TicketEntity> tickets = flightBooking.getTickets();

        return ticketAndBookingLogics.calculateRefund(
                flightBooking.getTotalPrice(),
                flightBooking.getFlight().getDepartureTime(),
                "Flight has already departed"
        );
    }

//    private @NonNull BigDecimal calculateRefund(Long ticketId) {
//        validationUtil.validateId(ticketId);
//
//        TicketEntity ticket = ticketRepository.findById(ticketId)
//                .orElseThrow();
//
//        BigDecimal refund = ticket.getPrice();
//
//        LocalDateTime departure = ticket.getFlight().getDepartureTime();
//        LocalDateTime now = LocalDateTime.now();
//
//        if (departure.isBefore(now)) {
//            throw new IllegalStateException("Flight has already departed");
//        }
//
//        long daysLeft = ChronoUnit.DAYS.between(now, departure);
//        BigDecimal res = ticketAndBookingLogics.getBigDecimal(daysLeft, refund);
//
//        return refund.subtract(res);
//    }


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

    public void expireUnpaidBookings() {

    }
}
