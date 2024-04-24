package com.bank.light.interfaces;

import com.bank.light.domain.Account;
import com.bank.light.domain.Transaction;
import java.util.List;

public interface TransactionService {
    Transaction deposit(Account account, Double amount);

    Transaction transfer(Account account, Double amount, Account receiver);

    Transaction withdraw(Account account, Double amount);

    Transaction get(Long id);

    List<Transaction> findByAccount(Account account);

    List<Transaction> findAllByPage(Integer page, String username, String purpose);

    List<Transaction> findAll(String username, String purpose);

    Long pageCount(String account, String purpose);

    void undo(Long id);

    void undo(String transferId);
}

