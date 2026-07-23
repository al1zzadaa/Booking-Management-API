package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.Rows;
import com.example.bookingmanagementapi.enums.Tickets;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor
@Setter
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id",  nullable = false)
    private FlightEntity flight;

    @Column(nullable = false)
    private String seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rows seatRow;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    private Tickets ticketClass;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(nullable = false,  updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
