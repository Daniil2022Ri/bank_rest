package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BankCardException {

    public InsufficientFundsException(Long cardId, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient funds on card %d. Available: %s, Requested: %s",
                cardId, available, requested));
    }
}

