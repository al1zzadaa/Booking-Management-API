package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.TokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TokenType tokenType =  TokenType.EMAIL_VERIFICATION;

    @Column(nullable = false,  updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;
}
