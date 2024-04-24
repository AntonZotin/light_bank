package com.bank.light.exceptions;

public class TransactionsNotConsistentException extends RuntimeException {
    public TransactionsNotConsistentException() {
        super("Transactions not consistent.");
    }
}
