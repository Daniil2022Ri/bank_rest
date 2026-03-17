package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder(16);

        // First digit (major industry identifier) - 4 for banking/financial
        sb.append("4");

        // Next 14 digits
        for (int i = 0; i < 14; i++) {
            sb.append(random.nextInt(10));
        }

        // Calculate and append Luhn check digit
        String partial = sb.toString();
        int checkDigit = calculateLuhnCheckDigit(partial);
        sb.append(checkDigit);

        return sb.toString();
    }

    private int calculateLuhnCheckDigit(String partial) {
        int sum = 0;
        boolean alternate = true;

        for (int i = partial.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(partial.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(cardNumber.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }
}

