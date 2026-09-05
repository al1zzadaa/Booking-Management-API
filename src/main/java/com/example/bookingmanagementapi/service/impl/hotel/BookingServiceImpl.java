package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.BookingStatus;
import com.example.bookingmanagementapi.enums.Hotels;
import com.example.bookingmanagementapi.event.BookingCancelledEvent;
import com.example.bookingmanagementapi.event.BookingPaymentEvent;
import com.example.bookingmanagementapi.exception.*;
import com.example.bookingmanagementapi.mapper.BookingMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.*;
import com.example.bookingmanagementapi.service.specifications.BookingSpecification;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    private final TicketAndBookingLogics ticketAndBookingLogics;
    private final LoyaltyPointService loyaltyPointService;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;

    @Override
    @Transactional
    public void bookHotel(String username, BookingRequest booking) {

//        validationUtil.validateId(booking.getUserId());
        validationUtil.validateId(booking.getAccountId());

        UserEntity userEntity = userRepository
                .findByEmail(username).orElseThrow(null);

        AccountEntity accountEntity = accountRepository.findById(booking.getAccountId()).orElseThrow(null);

        HotelEntity hotelEntity = hotelRepository.findById(booking.getHotel()).orElseThrow(null);

        RoomEntity roomEntity = roomRepository.findByIdForUpdate(booking.getRoom()).orElseThrow(null);

        userService.validateUserCanBook(userEntity.getId());
        accountService.validateAccountCanBook(booking.getAccountId());

        if (!accountEntity.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("Account does not belong to user");
        }

        if (!roomEntity.getHotel().getId().equals(hotelEntity.getId())) {
            throw new ValidationException("Room does not belong to the hotel");
        }

        if (hotelEntity.getStatus().equals(Hotels.CLOSED) || hotelEntity.getStatus().equals(Hotels.UNDER_RENOVATION)) {
            throw new HotelException("Hotel is closed or under renovation");
        }

        if (bookingRepository.existsByRoomIdAndBookingStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                booking.getRoom(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED),
                booking.getCheckOut(),
                booking.getCheckIn())) {

            throw new RoomException("Room is not available for these dates");
        }

        int childrenNumber = booking.getChildrenAges().size();


        if (roomEntity.getCapacity() < booking.getAdultNumber() + childrenNumber) {
            throw new CapacityException("People count is more than room capacity");
        }

        long days = ChronoUnit.DAYS.between(
                booking.getCheckIn().toLocalDate(),
                booking.getCheckOut().toLocalDate()
        );
        int adultNumber = booking.getAdultNumber();

        BigDecimal price = ticketAndBookingLogics.getTotalPrice(days, adultNumber, booking.getChildrenAges(), roomEntity);

        BookingEntity bookingEntity = BookingEntity.builder()
                .user(userEntity)
                .account(accountEntity)
                .hotel(hotelEntity)
                .room(roomEntity)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .bookingStatus(BookingStatus.PENDING)
                .totalPrice(price)
                .peopleNumber(adultNumber + childrenNumber)
                .paymentDeadline(LocalDateTime.now().plusMinutes(15))
                .build();


        bookingRepository.save(bookingEntity);

        log.info("Booking with id: '{}' has been booked for 15 minutes", bookingEntity.getId());

        notificationService.sendBookingNotification(userEntity.getId());
    }

    @Transactional
    @Override
    public void payBooking(String username, Long bookingId, PaymentRequest request) {


        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));


        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(null);


        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ValidationException(
                    "Flight booking does not belong to user"
            );
        }
        transactionService.payForBooking(booking, request);

        booking.setBookingStatus(BookingStatus.CONFIRMED);

//        bookingRepository.save(booking);

        loyaltyPointService.earnPoints(
                booking.getUser().getId(),
                booking.getTotalPrice(),
                "Points earned from hotel payment");

        log.info("Payment for booking with id: '{}'", bookingId);

//        notificationService.sendBookingPaymentNotification(booking);
        eventPublisher.publishEvent(
                new BookingPaymentEvent(user.getId())
        );
    }

    @Override
    @Transactional
    public void cancel(String username, Long bookingId) {

        validationUtil.validateId(bookingId);

        UserEntity userEntity = userRepository
                .findByEmail(username).orElseThrow(null);


        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("ticket not found"));

        if (!booking.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("Account does not belong to user");
        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException("booking not paid");
        }

        BigDecimal refund = calculateBookingRefund(bookingId);

        transactionService.refundBooking(booking, refund);

        booking.setBookingStatus(BookingStatus.CANCELLED);

        loyaltyPointService.cancelPoints(
                booking.getAccount(),
                booking.getUser().getId(),
                booking.getTotalPrice(),
                "Points removed due to booking cancellation"
        );

        log.info("Booking cancellation for booking with id: '{}'", bookingId);

//        notificationService.sendBookingCancellationNotification(booking.getUser().getId());
        eventPublisher.publishEvent(
                new BookingCancelledEvent(booking.getUser().getId())
        );
    }

    @Override
    public Page<@NonNull BookingResponse> getUserBookings(String username, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Page<@NonNull BookingEntity> bookings = bookingRepository.findAllByUserId(user.getId(), pageable);

        log.info("User bookings by userId {}", user.getId());

        return bookings.map(bookingMapper::toDto);
    }


    private BigDecimal calculateBookingRefund(Long bookingId) {
        validationUtil.validateId(bookingId);

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        return ticketAndBookingLogics.calculateRefund(
                booking.getTotalPrice(),
                booking.getCheckIn(),
                "Booking has already started"
        );
    }
//    private @NonNull BigDecimal calculateRefund(Long bookingId) {
//        validationUtil.validateId(bookingId);
//
//       BookingEntity booking = bookingRepository.findById(bookingId)
//               .orElseThrow(() -> new NotFoundException("ticket not found"));
//
//        BigDecimal refund = booking.getTotalPrice();
//
//        LocalDateTime departure = booking.getCheckIn();
//        LocalDateTime now = LocalDateTime.now();
//
//        if (departure.isBefore(now)) {
//            throw new IllegalStateException("Booking has already ended");
//        }
//
//        long daysLeft = ChronoUnit.DAYS.between(now, departure);
//        BigDecimal res = ticketAndBookingLogics.getBigDecimal(daysLeft, refund);
//
//        return refund.subtract(res);
//    }


    @Override
    public void deleteBooking(Long id) {
        //Todo

        bookingRepository.deleteById(id);
    }

    @Override
    public BookingResponse getBookingById(Long id) {

        BookingEntity booking = bookingRepository.findById(id).orElse(null);

        return bookingMapper.toDto(booking);
    }

    @Override
    public Page<@NonNull BookingResponse> getBookings(BookingFilter bookingFilter, Pageable pageable) {

        var specification = new BookingSpecification(bookingFilter);

        Page<@NonNull BookingEntity> bookingEntities = bookingRepository.findAll(specification, pageable);

        return bookingEntities.map(bookingMapper::toDto);
    }

    @Override
    public void cancelBooking(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId).orElseThrow(null);

        booking.setBookingStatus(BookingStatus.CANCELLED);
    }
}
