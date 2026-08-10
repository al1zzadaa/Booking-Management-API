package com.example.bookingmanagementapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscriptions")
@Builder
@AllArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id",  nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_renew_account_id")
    private AccountEntity autoRenewAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id",  nullable = false)
    private SubscriptionPlanEntity subscriptionPlan;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive =  true;
}
