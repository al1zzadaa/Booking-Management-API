package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.Flights;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flights")
public class FlightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String departureCountry;

    @Column(nullable = false)
    private String departureCity;

    @Column(nullable = false)
    private String arrivalCountry;

    @Column(nullable = false)
    private String arrivalCity;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airline_id",  nullable = false)
    private AirlineEntity airline;

    @Column(nullable = false)
    private Long flightNumber;

    @Enumerated(EnumType.STRING)
    private Flights status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
