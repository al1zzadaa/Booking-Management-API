package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.PaymentRequest;
import com.example.bookingmanagementapi.dto.request.UpdateBookingRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.entity.*;
import com.example.bookingmanagementapi.enums.BookingStatus;
import com.example.bookingmanagementapi.enums.Hotels;
import com.example.bookingmanagementapi.exception.*;
import com.example.bookingmanagementapi.mapper.BookingMapper;
import com.example.bookingmanagementapi.repository.*;
import com.example.bookingmanagementapi.service.*;
import com.example.bookingmanagementapi.service.impl.TicketAndBookingLogicsImpl;
import com.example.bookingmanagementapi.service.specifications.BookingSpecification;
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
//    @Value("${adultPrice}")
//    private Integer adultPrice;
//    @Value("${childPrice}")
//    private Integer childPrice;
//    @Value("${percent10}")
//    private Integer percent10;
//    @Value("${percent20}")
//    private Integer percent20;
//    @Value("${percent30}")
//    private Integer percent30;
//    @Value("${percent50}")
//    private Integer percent50;
//    @Value("${percent70}")
//    private Integer percent70;

    @Override
    public void bookHotel(BookingRequest booking) {

        validationUtil.validateId(booking.getUserId());
        validationUtil.validateId(booking.getAccountId());

        UserEntity userEntity = userRepository.findById(booking.getUserId()).orElseThrow(null);

        AccountEntity accountEntity = accountRepository.findById(booking.getAccountId()).orElseThrow(null);

        HotelEntity hotelEntity = hotelRepository.findById(booking.getHotel()).orElseThrow(null);

        RoomEntity roomEntity = roomRepository.findById(booking.getRoom()).orElseThrow(null);

        userService.validateUserCanBook(booking.getUserId());


        if (!roomEntity.getHotel().getId().equals(hotelEntity.getId())) {
            throw new ValidationException("Room does not belong to the hotel");
        }

        if (hotelEntity.getStatus().equals(Hotels.CLOSED) || hotelEntity.getStatus().equals(Hotels.UNDER_RENOVATION)) {
            throw new HotelException("Hotel is closed or under renovation");
        }

        if (bookingRepository.existsByRoomIdAndCheckInLessThanAndCheckOutGreaterThan(
                        booking.getRoom(),
                        booking.getCheckOut(),
                        booking.getCheckIn())) {
            throw new RoomException("Room is not available for these dates");
        }


        if (roomEntity.getCapacity() < booking.getAdultNumber() + booking.getChildrenNumber()) {
            throw new CapacityException("People count is more than room capacity");
        }

        int days = booking.getCheckOut().getDayOfMonth() - booking.getCheckIn().getDayOfMonth();
        int adultNumber = booking.getAdultNumber();
        int childNumber = booking.getChildrenNumber();
        BigDecimal pricePerNight = roomEntity.getPricePerNight();

        BigDecimal price = ticketAndBookingLogics.getTotalPrice(days, adultNumber, childNumber, pricePerNight);

        BookingEntity bookingEntity = BookingEntity.builder()
                .user(userEntity)
                .account(accountEntity)
                .hotel(hotelEntity)
                .room(roomEntity)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .bookingStatus(BookingStatus.PENDING)
                .totalPrice(price)
                .peopleNumber(adultNumber + childNumber)
                .build();


        bookingRepository.save(bookingEntity);

        notificationService.sendBookingNotification(userEntity.getId());
    }

    @Transactional
    @Override
    public void payBooking(Long bookingId, PaymentRequest request) {

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(null);

        transactionService.payForBooking(booking, request);

        booking.setBookingStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        notificationService.sendBookingPaymentNotification(booking);
    }

    @Override
    @Transactional
    public void cancel(Long bookingId) {

        validationUtil.validateId(bookingId);

        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("ticket not found"));


        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new ValidationException("booking not paid");
        }

        BigDecimal refund = calculateRefund(bookingId);

        transactionService.refundBooking(booking, refund);

        booking.setBookingStatus(BookingStatus.CANCELLED);


        notificationService.sendBookingCancellationNotification(booking);
    }


    private BigDecimal calculateRefund(Long bookingId) {
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
    public void updateBooking(UpdateBookingRequest updateBookingRequest, Long id) {
        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(null);

        bookingMapper.updateBooking(updateBookingRequest, bookingEntity);

        bookingRepository.save(bookingEntity);

    }

    @Override
    public void deleteBooking(Long id) {
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
