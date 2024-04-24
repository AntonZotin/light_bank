package com.bank.light.interfaces;

import com.bank.light.domain.Account;
import java.util.List;

public interface AccountService {
    void deposit(Account account, Double amount);

    void transfer(Account account, Double amount, Account receiver);

    void withdraw(Account account, Double amount);

    void undoTransaction(Account account, Long transactionId);

    List<String> listAccountNames();

    String getUsername(String paymentAccount);
}
