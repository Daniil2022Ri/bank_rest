package com.example.bankcards.exception;

public class BankCardException extends RuntimeException {

    public BankCardException(String message) {
        super(message);
    }

    public BankCardException(String message, Throwable cause) {
        super(message, cause);
    }
}


