package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.Rooms;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id", nullable = false)
    private HotelEntity hotel;

    private Integer roomNumber;

    @Column(name = "name",  nullable = false)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rooms roomType;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "adult_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal adultPrice;

    @Column(name = "child_discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal childDiscountPercent;

    @Column(name = "capacity",   nullable = false)
    private Integer capacity;

    private String description;

    @CreationTimestamp
    @Column(nullable = false,  updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
