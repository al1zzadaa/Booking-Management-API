package com.example.bookingmanagementapi.service.impl.hotel;

import com.example.bookingmanagementapi.dto.filter.BookingFilter;
import com.example.bookingmanagementapi.dto.request.BookingRequest;
import com.example.bookingmanagementapi.dto.request.UpdateBookingRequest;
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse;
import com.example.bookingmanagementapi.entity.BookingEntity;
import com.example.bookingmanagementapi.entity.HotelEntity;
import com.example.bookingmanagementapi.entity.RoomEntity;
import com.example.bookingmanagementapi.enums.BookingStatus;
import com.example.bookingmanagementapi.enums.Hotels;
import com.example.bookingmanagementapi.mapper.BookingMapper;
import com.example.bookingmanagementapi.repository.BookingRepository;
import com.example.bookingmanagementapi.repository.HotelRepository;
import com.example.bookingmanagementapi.repository.RoomRepository;
import com.example.bookingmanagementapi.service.BookingService;
import com.example.bookingmanagementapi.service.specifications.BookingSpecification;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    public void bookHotel(BookingRequest booking) {

        //TODO USER

        HotelEntity hotelEntity = hotelRepository.findById(booking.getHotel()).orElseThrow(null);

        RoomEntity roomEntity = roomRepository.findById(booking.getRoom()).orElseThrow(null);

        if (!roomEntity.getHotel().getId().equals(hotelEntity.getId())) {
            //
        }

        if (hotelEntity.getStatus().equals(Hotels.CLOSED) || hotelEntity.getStatus().equals(Hotels.UNDER_RENOVATION)) {
            //exception
        }

        if (roomEntity.getCapacity() < booking.getPeopleNumber()){
            //
        }

        BookingEntity bookingEntity = new BookingEntity();

        int days = booking.getCheckOut().getDayOfMonth()-booking.getCheckIn().getDayOfMonth();

//        bookingEntity.setUserId();
        bookingEntity.setHotel(hotelEntity);
        bookingEntity.setRoom(roomEntity);
        bookingEntity.setCheckIn(booking.getCheckIn());
        bookingEntity.setCheckOut(booking.getCheckOut());
        bookingEntity.setBookingStatus(BookingStatus.PENDING);
        bookingEntity.setTotalPrice(roomEntity.getPricePerNight().multiply(BigDecimal.valueOf(days)));

        bookingRepository.save(bookingEntity);

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
