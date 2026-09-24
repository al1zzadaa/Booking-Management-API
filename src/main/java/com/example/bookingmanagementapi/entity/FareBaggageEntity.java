package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.TicketClass;
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
@Table(name = "fare_baggage")
public class FareBaggageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airline_id",  nullable = false)
    private AirlineEntity airline;

    @Column(nullable = false)
    private Integer baggage;

    @Column(nullable = false)
    private Integer handLuggage;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private TicketClass ticketClass;

    @CreationTimestamp
    @Column(nullable = false,  updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;
}