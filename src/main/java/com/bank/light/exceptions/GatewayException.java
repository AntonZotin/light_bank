package com.bank.light.exceptions;

public class GatewayException extends RuntimeException {
    public GatewayException(String message) {
        super("Gateway service exception: " + message);
    }
}
