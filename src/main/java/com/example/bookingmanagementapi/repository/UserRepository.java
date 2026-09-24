package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<@NonNull UserEntity,@NonNull Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByAccountsId(Long accountId);
}
