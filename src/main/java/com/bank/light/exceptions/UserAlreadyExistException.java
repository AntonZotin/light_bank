package com.bank.light.exceptions;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String username) {
        super("There is an account with that username: " + username);
    }
}
