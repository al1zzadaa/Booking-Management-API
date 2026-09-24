package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.TransactionEntity;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<@NonNull TransactionEntity, @NonNull Long> {

    Page<@NonNull TransactionEntity> findAllByAccount(AccountEntity account,  Pageable pageable);

    boolean existsByReferenceIdAndReferenceTypeAndType(
            Long referenceId,
            ReferenceType referenceType,
            TransactionType type
    );

    Page<@NonNull TransactionEntity> findAllByAccount_User_Email(String accountUserEmail, Pageable pageable);
}
