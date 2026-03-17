package com.example.bankcards.exception;

public class CardBlockedException extends BankCardException {

    public CardBlockedException(Long cardId) {
        super("Card is blocked: " + cardId);
    }
}

