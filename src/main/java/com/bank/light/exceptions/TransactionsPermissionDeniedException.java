package com.bank.light.exceptions;

public class TransactionsPermissionDeniedException extends RuntimeException {
    public TransactionsPermissionDeniedException() {
        super("You do not have permission on this transaction.");
    }
}
