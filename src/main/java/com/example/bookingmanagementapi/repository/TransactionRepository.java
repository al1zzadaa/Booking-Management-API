package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.TransactionEntity;
import com.example.bookingmanagementapi.enums.PaymentStatus;
import com.example.bookingmanagementapi.enums.ReferenceType;
import com.example.bookingmanagementapi.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    Page<TransactionEntity> findAllByAccount(AccountEntity account,  Pageable pageable);

//    boolean existsByReferenceIdAndReferenceTypeAndPaymentStatus(
//            Long referenceId,
//            ReferenceType referenceType,
//            PaymentStatus paymentStatus
//    );

//    boolean existsByReferenceIdAndReferenceTypeAndTypeAndPaymentStatus(
//            Long referenceId,
//            ReferenceType referenceType,
//            TransactionType transactionType,
//            PaymentStatus paymentStatus);

    boolean existsByReferenceIdAndReferenceTypeAndType(
            Long referenceId,
            ReferenceType referenceType,
            TransactionType type
    );
}
