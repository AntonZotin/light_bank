package com.bank.light.exceptions;

public class TransactionsNotFoundException extends RuntimeException {
    public TransactionsNotFoundException() {
        super("Transactions not found.");
    }
}
