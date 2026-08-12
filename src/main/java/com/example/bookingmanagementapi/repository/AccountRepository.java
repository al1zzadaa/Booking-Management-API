package com.example.bookingmanagementapi.repository;

import com.example.bookingmanagementapi.entity.AccountEntity;
import com.example.bookingmanagementapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long>,
        JpaSpecificationExecutor<AccountEntity> {


    AccountEntity findByUserId(Long userId);



//    Page<AccountEntity> findAll(AccountFilter accountFilter, Pageable pageable);


    List<AccountEntity> user(UserEntity user);
}
