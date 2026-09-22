package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.filter.BookingFilter
import com.example.bookingmanagementapi.dto.request.BookingRequest
import com.example.bookingmanagementapi.dto.request.PaymentRequest
import com.example.bookingmanagementapi.dto.response.hotel.BookingResponse
import com.example.bookingmanagementapi.entity.*
import com.example.bookingmanagementapi.enums.BookingStatus
import com.example.bookingmanagementapi.enums.Hotels
import com.example.bookingmanagementapi.event.BookingCancelledEvent
import com.example.bookingmanagementapi.event.BookingPaymentEvent
import com.example.bookingmanagementapi.exception.*
import com.example.bookingmanagementapi.mapper.BookingMapper
import com.example.bookingmanagementapi.repository.*
import com.example.bookingmanagementapi.service.impl.BookingServiceImpl
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

import java.time.LocalDateTime

class BookingServiceTest extends Specification {

    def bookingMapper = Mock(BookingMapper)
    def bookingRepository = Mock(BookingRepository)
    def hotelRepository = Mock(HotelRepository)
    def roomRepository = Mock(RoomRepository)
    def userRepository = Mock(UserRepository)
    def notificationService = Mock(NotificationService)
    def accountRepository = Mock(AccountRepository)
    def transactionService = Mock(TransactionService)
    def userService = Mock(UserService)
    def loyaltyPointService = Mock(LoyaltyPointService)
    def eventPublisher = Mock(ApplicationEventPublisher)
    def accountService = Mock(AccountService)
    def calculationService = Mock(CalculationService)

    def bookingService = new BookingServiceImpl(
            bookingMapper,
            bookingRepository,
            hotelRepository,
            roomRepository,
            userRepository,
            notificationService,
            accountRepository,
            transactionService,
            userService,
            loyaltyPointService,
            eventPublisher,
            accountService,
            calculationService
    )

    def "bookHotel should create pending booking successfully"() {
        given:
        def user = new UserEntity()
        user.setId(1L)
        user.setEmail("john@gmail.com")

        def account = new AccountEntity()
        account.setId(10L)
        account.setUser(user)

        def hotel = new HotelEntity()
        hotel.setId(20L)
        hotel.setStatus(Hotels.OPEN)

        def room = new RoomEntity()
        room.setId(30L)
        room.setHotel(hotel)
        room.setCapacity(3)

        def request = new BookingRequest()
        request.setAccountId(10L)
        request.setHotel(20L)
        request.setRoom(30L)
        request.setAdultNumber(2)
        request.setChildrenAges([5])
        request.setCheckIn(LocalDateTime.of(2026, 10, 10, 14, 0))
        request.setCheckOut(LocalDateTime.of(2026, 10, 13, 12, 0))

        def price = new BigDecimal("380.00")

        when:
        bookingService.bookHotel("john@gmail.com", request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * hotelRepository.findById(20L) >> Optional.of(hotel)
        1 * roomRepository.findByIdForUpdate(30L) >> Optional.of(room)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * bookingRepository.existsByRoomIdAndBookingStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                30L,
                [BookingStatus.PENDING, BookingStatus.CONFIRMED],
                request.getCheckOut(),
                request.getCheckIn()
        ) >> false

        1 * calculationService.getBookingTotalPrice(
                3L,
                2,
                [5],
                room
        ) >> price

        1 * bookingRepository.save({
            BookingEntity booking ->
                booking.getUser() == user &&
                        booking.getAccount() == account &&
                        booking.getHotel() == hotel &&
                        booking.getRoom() == room &&
                        booking.getCheckIn() == request.getCheckIn() &&
                        booking.getCheckOut() == request.getCheckOut() &&
                        booking.getBookingStatus() == BookingStatus.PENDING &&
                        booking.getTotalPrice() == price &&
                        booking.getPeopleNumber() == 3 &&
                        booking.getPaymentDeadline() != null
        })

        1 * notificationService.sendBookingNotification(1L)
    }

    def "bookHotel should throw AccessDeniedException when account does not belong to user"() {
        given:
        def user = new UserEntity()
        user.setId(1L)

        def anotherUser = new UserEntity()
        anotherUser.setId(2L)

        def account = new AccountEntity()
        account.setId(10L)
        account.setUser(anotherUser)

        def hotel = new HotelEntity()
        hotel.setId(20L)

        def room = new RoomEntity()
        room.setId(30L)
        room.setHotel(hotel)

        def request = new BookingRequest()
        request.setAccountId(10L)
        request.setHotel(20L)
        request.setRoom(30L)

        when:
        bookingService.bookHotel("john@gmail.com", request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * hotelRepository.findById(20L) >> Optional.of(hotel)
        1 * roomRepository.findByIdForUpdate(30L) >> Optional.of(room)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        thrown(AccessDeniedException)

        0 * bookingRepository.save(_)
        0 * notificationService._
    }

    def "bookHotel should throw ValidationException when room does not belong to hotel"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def hotel = new HotelEntity(id: 20L)

        def anotherHotel = new HotelEntity(id: 99L)

        def room = new RoomEntity(id: 30L)
        room.setHotel(anotherHotel)

        def request = new BookingRequest()
        request.setAccountId(10L)
        request.setHotel(20L)
        request.setRoom(30L)

        when:
        bookingService.bookHotel("john@gmail.com", request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * hotelRepository.findById(20L) >> Optional.of(hotel)
        1 * roomRepository.findByIdForUpdate(30L) >> Optional.of(room)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        def exception = thrown(ValidationException)
        exception.message == "Room does not belong to the hotel"

        0 * bookingRepository.save(_)
    }

    def "bookHotel should throw HotelException when hotel is closed"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def hotel = new HotelEntity(id: 20L)
        hotel.setStatus(Hotels.CLOSED)

        def room = new RoomEntity(id: 30L)
        room.setHotel(hotel)

        def request = new BookingRequest()
        request.setAccountId(10L)
        request.setHotel(20L)
        request.setRoom(30L)

        when:
        bookingService.bookHotel("john@gmail.com", request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * hotelRepository.findById(20L) >> Optional.of(hotel)
        1 * roomRepository.findByIdForUpdate(30L) >> Optional.of(room)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        def exception = thrown(HotelException)
        exception.message == "Hotel is closed or under renovation"

        0 * bookingRepository.save(_)
    }

    def "bookHotel should throw RoomException when room is unavailable"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def hotel = new HotelEntity(id: 20L)
        hotel.setStatus(Hotels.OPEN)

        def room = new RoomEntity(id: 30L)
        room.setHotel(hotel)

        def request = new BookingRequest()
        request.setAccountId(10L)
        request.setHotel(20L)
        request.setRoom(30L)
        request.setAdultNumber(1)
        request.setChildrenAges([])
        request.setCheckIn(LocalDateTime.of(2026, 10, 10, 14, 0))
        request.setCheckOut(LocalDateTime.of(2026, 10, 13, 12, 0))

        when:
        bookingService.bookHotel("john@gmail.com", request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * hotelRepository.findById(20L) >> Optional.of(hotel)
        1 * roomRepository.findByIdForUpdate(30L) >> Optional.of(room)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * bookingRepository.existsByRoomIdAndBookingStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                30L,
                [BookingStatus.PENDING, BookingStatus.CONFIRMED],
                request.getCheckOut(),
                request.getCheckIn()
        ) >> true

        def exception = thrown(RoomException)
        exception.message == "Room is not available for these dates"

        0 * calculationService._
        0 * bookingRepository.save(_)
    }

    def "bookHotel should throw CapacityException when people exceed room capacity"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)
        account.setUser(user)

        def hotel = new HotelEntity(id: 20L)
        hotel.setStatus(Hotels.OPEN)

        def room = new RoomEntity(id: 30L)
        room.setHotel(hotel)
        room.setCapacity(2)

        def request = new BookingRequest()
        request.setAccountId(10L)
        request.setHotel(20L)
        request.setRoom(30L)
        request.setAdultNumber(2)
        request.setChildrenAges([5])

        when:
        bookingService.bookHotel("john@gmail.com", request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * accountRepository.findById(10L) >> Optional.of(account)
        1 * hotelRepository.findById(20L) >> Optional.of(hotel)
        1 * roomRepository.findByIdForUpdate(30L) >> Optional.of(room)

        1 * userService.validateUserCanBook(1L)
        1 * accountService.validateAccountCanBook(10L)

        1 * bookingRepository.existsByRoomIdAndBookingStatusInAndCheckInLessThanAndCheckOutGreaterThan(
                30L,
                [BookingStatus.PENDING, BookingStatus.CONFIRMED],
                _,
                _
        ) >> false

        def exception = thrown(CapacityException)
        exception.message == "People count is more than room capacity"

        0 * calculationService._
        0 * bookingRepository.save(_)
    }

    def "payBooking should pay booking and confirm it"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking = new BookingEntity()
        booking.setUser(user)
        booking.setTotalPrice(new BigDecimal("200"))

        def request = new PaymentRequest()

        when:
        bookingService.payBooking("john@gmail.com", 50L, request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * bookingRepository.findById(50L) >> Optional.of(booking)

        1 * transactionService.payForBooking(booking, request)

        booking.getBookingStatus() == BookingStatus.CONFIRMED

        1 * loyaltyPointService.earnPoints(
                1L,
                new BigDecimal("200"),
                "Points earned from hotel payment"
        )

        1 * eventPublisher.publishEvent({
            it instanceof BookingPaymentEvent &&
                    it.userId() == 1L
        })
    }

    def "payBooking should throw ValidationException when booking belongs to another user"() {
        given:
        def user = new UserEntity(id: 1L)
        def anotherUser = new UserEntity(id: 2L)

        def booking = new BookingEntity()
        booking.setUser(anotherUser)

        when:
        bookingService.payBooking(
                "john@gmail.com",
                50L,
                new PaymentRequest()
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * bookingRepository.findById(50L) >> Optional.of(booking)

        def exception = thrown(ValidationException)
        exception.message == "Flight booking does not belong to user"

        0 * transactionService._
        0 * loyaltyPointService._
        0 * eventPublisher._
    }

    def "cancel should refund confirmed booking and cancel it"() {
        given:
        def user = new UserEntity(id: 1L)

        def account = new AccountEntity(id: 10L)

        def booking = new BookingEntity()
        booking.setUser(user)
        booking.setAccount(account)
        booking.setBookingStatus(BookingStatus.CONFIRMED)
        booking.setTotalPrice(new BigDecimal("200"))

        def refund = new BigDecimal("180")

        when:
        bookingService.cancel("john@gmail.com", 50L)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * bookingRepository.findById(50L) >> Optional.of(booking)

        1 * calculationService.calculateBookingRefund(50L) >> refund

        1 * transactionService.refundBooking(booking, refund)

        booking.getBookingStatus() == BookingStatus.CANCELLED

        1 * loyaltyPointService.cancelPoints(
                account,
                1L,
                new BigDecimal("200"),
                "Points removed due to booking cancellation"
        )

        1 * eventPublisher.publishEvent({
            it instanceof BookingCancelledEvent &&
                    it.userId() == 1L
        })
    }

    def "cancel should throw AccessDeniedException when booking belongs to another user"() {
        given:
        def user = new UserEntity(id: 1L)
        def anotherUser = new UserEntity(id: 2L)

        def booking = new BookingEntity()
        booking.setUser(anotherUser)

        when:
        bookingService.cancel("john@gmail.com", 50L)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * bookingRepository.findById(50L) >> Optional.of(booking)

        thrown(AccessDeniedException)

        0 * calculationService._
        0 * transactionService._
        0 * loyaltyPointService._
        0 * eventPublisher._
    }

    def "cancel should throw ValidationException when booking is not confirmed"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking = new BookingEntity()
        booking.setUser(user)
        booking.setBookingStatus(BookingStatus.PENDING)

        when:
        bookingService.cancel("john@gmail.com", 50L)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * bookingRepository.findById(50L) >> Optional.of(booking)

        def exception = thrown(ValidationException)
        exception.message == "booking not paid"

        0 * calculationService._
        0 * transactionService._
        0 * loyaltyPointService._
    }

    def "getUserBookings should return mapped user bookings"() {
        given:
        def user = new UserEntity(id: 1L)

        def booking1 = new BookingEntity()
        def booking2 = new BookingEntity()

        def response1 = new BookingResponse()
        def response2 = new BookingResponse()

        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<BookingEntity>(
                [booking1, booking2],
                pageable,
                2
        )

        when:
        def result = bookingService.getUserBookings(
                "john@gmail.com",
                pageable
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * bookingRepository.findAllByUserId(1L, pageable) >> page

        1 * bookingMapper.toDto(booking1) >> response1
        1 * bookingMapper.toDto(booking2) >> response2

        result.content == [response1, response2]
    }

    def "getUserBookings should throw NotFoundException when user does not exist"() {
        when:
        bookingService.getUserBookings(
                "john@gmail.com",
                PageRequest.of(0, 10)
        )

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        def exception = thrown(NotFoundException)
        exception.message == "User not found"

        0 * bookingRepository._
    }

    def "deleteBooking should delete booking when it exists"() {
        when:
        bookingService.deleteBooking(50L)

        then:
        1 * bookingRepository.existsById(50L) >> true
        1 * bookingRepository.deleteById(50L)
    }

    def "deleteBooking should throw NotFoundException when booking does not exist"() {
        when:
        bookingService.deleteBooking(50L)

        then:
        1 * bookingRepository.existsById(50L) >> false

        def exception = thrown(NotFoundException)
        exception.message == "Booking not found"

        0 * bookingRepository.deleteById(_)
    }

    def "getBookingById should return booking response"() {
        given:
        def booking = new BookingEntity()
        def response = new BookingResponse()

        when:
        def result = bookingService.getBookingById(50L)

        then:
        1 * bookingRepository.findById(50L) >> Optional.of(booking)
        1 * bookingMapper.toDto(booking) >> response

        result == response
    }

    def "getBookings should return mapped bookings"() {
        given:
        def filter = new BookingFilter()
        def pageable = PageRequest.of(0, 10)

        def booking1 = new BookingEntity()
        def booking2 = new BookingEntity()

        def response1 = new BookingResponse()
        def response2 = new BookingResponse()

        def page = new PageImpl<BookingEntity>(
                [booking1, booking2],
                pageable,
                2
        )

        when:
        def result = bookingService.getBookings(filter, pageable)

        then:
        1 * bookingRepository.findAll(_, pageable) >> page

        1 * bookingMapper.toDto(booking1) >> response1
        1 * bookingMapper.toDto(booking2) >> response2

        result.content == [response1, response2]
    }

    def "getBookings should return empty page when no bookings exist"() {
        given:
        def filter = new BookingFilter()
        def pageable = PageRequest.of(0, 10)

        def page = new PageImpl<BookingEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = bookingService.getBookings(filter, pageable)

        then:
        1 * bookingRepository.findAll(_, pageable) >> page

        0 * bookingMapper.toDto(_)

        result.empty
    }

    def "cancelBooking should set booking status to cancelled"() {
        given:
        def booking = new BookingEntity()
        booking.setBookingStatus(BookingStatus.CONFIRMED)

        when:
        bookingService.cancelBooking(50L)

        then:
        1 * bookingRepository.findById(50L) >> Optional.of(booking)

        booking.getBookingStatus() == BookingStatus.CANCELLED
    }
}