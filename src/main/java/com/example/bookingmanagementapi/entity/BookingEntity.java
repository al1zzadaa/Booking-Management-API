package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id", nullable = false)
    private HotelEntity hotel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @Column(nullable = false)
    private LocalDateTime checkIn;

    @Column(nullable = false)
    private LocalDateTime checkOut;

    @Column(nullable = false)
    private Integer peopleNumber;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    private LocalDateTime paymentDeadline;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
