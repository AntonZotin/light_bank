package com.bank.light.exceptions;

public class BalanceNotNullException extends RuntimeException {
    public BalanceNotNullException() {
        super("You have money on balance yet.");
    }
}
