package com.example.bookingmanagementapi.entity;

import com.example.bookingmanagementapi.enums.Roles;
import com.example.bookingmanagementapi.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@Entity
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "user",  fetch = FetchType.LAZY,  cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountEntity> accounts = new HashSet<>();;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus isActive = UserStatus.NOT_VERIFIED;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "roles" , nullable = false)
    private Set<Roles> roles;

    private LocalDateTime emailVerifiedAt;

    @CreationTimestamp
    @Column(updatable = false,  nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;
}
