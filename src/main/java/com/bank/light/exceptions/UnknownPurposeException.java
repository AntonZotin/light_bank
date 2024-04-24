package com.bank.light.exceptions;

public class UnknownPurposeException extends RuntimeException {
    public UnknownPurposeException(String purpose) {
        super("Transaction purpose is unknown: " + purpose);
    }
}
