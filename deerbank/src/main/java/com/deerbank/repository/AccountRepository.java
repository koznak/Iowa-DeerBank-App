package com.deerbank.repository;

import com.deerbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByAccountNo(String accountNo);
    boolean existsByAccountNo(String accountNo);
    Optional<Account> findByAccountIdAndStatus(int accountId,String status);
    Optional<Account> findByAccountNoAndStatus(String accountNo, String status);
}
