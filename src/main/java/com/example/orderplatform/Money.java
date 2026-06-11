package com.example.orderplatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monetary value object used by pricing, orders and payments.
 *
 * <p>Amounts are normalized to two decimal places and currencies are stored as uppercase ISO-4217
 * codes. Negative amounts and mixed-currency arithmetic are rejected as business rule violations.
 *
 * @param amount decimal amount, rounded half up to currency-style precision
 * @param currency three-letter currency code
 */
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

    /**
     * Creates a normalized money value.
     *
     * @param amount decimal amount
     * @param currency three-letter currency code
     * @return normalized money value
     */
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    /**
     * Creates a zero amount in the requested currency.
     *
     * @param currency three-letter currency code
     * @return zero money value
     */
    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Adds two monetary values after verifying they use the same currency.
     *
     * @param other value to add
     * @return sum of both values
     * @throws BusinessRuleViolationException when currencies differ
     */
    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    /**
     * Multiplies this value by a positive quantity.
     *
     * @param factor positive multiplier
     * @return multiplied money value
     * @throws BusinessRuleViolationException when the multiplier is less than one
     */
    public Money multiply(int factor) {
        if (factor < 1) {
            throw new BusinessRuleViolationException("Quantity must be greater than zero.");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    /**
     * Verifies that another monetary value uses the same currency.
     *
     * @param other value to compare
     * @throws BusinessRuleViolationException when currencies differ
     */
    public void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new BusinessRuleViolationException("All monetary values must use the same currency.");
        }
    }
}
