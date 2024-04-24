package com.bank.light.repositories;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);

    List<Transaction> findByTransferId(String transferId);

    Page<Transaction> findAllByAccount_User_Username(String username, Pageable pageable);

    Page<Transaction> findAllByPurpose(String purpose, Pageable pageable);

    Page<Transaction> findAllByAccount_User_UsernameAndPurpose(String username, String purpose, Pageable pageable);

    List<Transaction> findAllByAccount_User_Username(String username);

    List<Transaction> findAllByPurpose(String purpose);

    List<Transaction> findAllByAccount_User_UsernameAndPurpose(String username, String purpose);

    Long countByAccount_User_Username(String username);

    Long countByPurpose(String purpose);

    Long countByAccount_User_UsernameAndPurpose(String username, String purpose);

    Long countByAccountAndCreatedAtAfter(Account account, LocalDateTime limit);
}
