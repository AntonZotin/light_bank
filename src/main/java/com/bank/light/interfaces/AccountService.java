package com.bank.light.interfaces;

import java.util.List;

public interface AccountService {
    void deposit(String username, Double amount);

    void transfer(String username, Double amount, String receiverUsername);

    void withdraw(String username, Double amount);

    void undoTransaction(String username, Long transactionId);

    List<String> listAccountNames();

    String getUsername(String paymentAccount);
}
