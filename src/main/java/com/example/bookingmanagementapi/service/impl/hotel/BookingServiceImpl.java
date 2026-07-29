package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.UpdateBookingRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.entity.HotelEntity;
import com.example.bookingmanagementapi.entity.RoomEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import com.example.bookingmanagementapi.enums.BookingStatus;
import com.example.bookingmanagementapi.enums.Hotels;
import com.example.bookingmanagementapi.exception.CapacityException;
import com.example.bookingmanagementapi.exception.HotelException;
import com.example.bookingmanagementapi.exception.RoomException;
import com.example.bookingmanagementapi.exception.ValidationException;
import com.example.bookingmanagementapi.mapper.BookingMapper;
import com.example.bookingmanagementapi.repository.BookingRepository;
import com.example.bookingmanagementapi.repository.HotelRepository;
import com.example.bookingmanagementapi.repository.RoomRepository;
import com.example.bookingmanagementapi.repository.UserRepository;
import com.example.bookingmanagementapi.service.BookingService;
import com.example.bookingmanagementapi.service.NotificationService;
import com.example.bookingmanagementapi.service.UserService;
import com.example.bookingmanagementapi.service.specifications.BookingSpecification;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
    private final UserService userService;
    @Value("${adultPrice}")
    private Integer adultPrice;
    @Value("${childPrice}")
    private Integer childPrice;

    @Override
    public void bookHotel(BookingRequest booking) {

        validationUtil.validateId(booking.getUserId());

        UserEntity userEntity = userRepository.findById(booking.getUserId()).orElseThrow(null);

        HotelEntity hotelEntity = hotelRepository.findById(booking.getHotel()).orElseThrow(null);

        RoomEntity roomEntity = roomRepository.findById(booking.getRoom()).orElseThrow(null);

        userService.validateUserCanBook(booking.getUserId());


        if (!roomEntity.getHotel().getId().equals(hotelEntity.getId())) {
            throw new ValidationException("Room does not belong to the hotel");
        }

        if (hotelEntity.getStatus().equals(Hotels.CLOSED) || hotelEntity.getStatus().equals(Hotels.UNDER_RENOVATION)) {
            throw new HotelException("Hotel is closed or under renovation");
        }

        if (bookingRepository.existsByRoomIdAndCheckInLessThanAndCheckOutGreaterThan(booking.getRoom(), booking.getCheckIn(), booking.getCheckOut())){
            throw new RoomException("Room has already been booked");
        }


        if (roomEntity.getCapacity() < booking.getAdultNumber() + booking.getChildrenNumber()) {
            throw new CapacityException("People count is more than room capacity");
        }

        int days = booking.getCheckOut().getDayOfMonth() - booking.getCheckIn().getDayOfMonth();
        int adultNumber = booking.getAdultNumber();
        int childNumber = booking.getChildrenNumber();
        BigDecimal pricePerNight = roomEntity.getPricePerNight();

        BigDecimal price = getTotalPrice(days, adultNumber, childNumber, pricePerNight);

        BookingEntity bookingEntity = BookingEntity.builder()
                .user(userEntity)
                .hotel(hotelEntity)
                .room(roomEntity)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .bookingStatus(BookingStatus.PENDING)
                .totalPrice(price)
                .build();


        bookingRepository.save(bookingEntity);

        notificationService.sendBookingNotification(userEntity.getId());
    }

    private BigDecimal getTotalPrice(Integer days, Integer adultNumber, Integer childNumber, BigDecimal pricePerNight) {

        BigDecimal adultTotal = BigDecimal.valueOf(adultPrice).multiply(BigDecimal.valueOf(adultNumber));
        BigDecimal childTotal = BigDecimal.valueOf(childPrice).multiply(BigDecimal.valueOf(childNumber));
        BigDecimal priceForDays = BigDecimal.valueOf(days).multiply(pricePerNight);

        return adultTotal.add(childTotal).add(priceForDays);
    }

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
