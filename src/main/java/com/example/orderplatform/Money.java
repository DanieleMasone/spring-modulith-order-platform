package com.example.orderplatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        if (currency.length() != 3) {
            throw new BusinessRuleViolationException("Currency must use the ISO-4217 three-letter code.");
        }
        if (amount.signum() < 0) {
            throw new BusinessRuleViolationException("Money amount cannot be negative.");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int factor) {
        if (factor < 1) {
            throw new BusinessRuleViolationException("Quantity must be greater than zero.");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new BusinessRuleViolationException("All monetary values must use the same currency.");
        }
    }
}
