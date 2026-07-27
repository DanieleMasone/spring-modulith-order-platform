package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalizesAmountAndCurrency() {
        Money money = Money.of(new BigDecimal("14.995"), " eur ");

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(money.currency()).isEqualTo("EUR");
    }

    @Test
    void addsAndMultipliesValuesInTheSameCurrency() {
        Money price = Money.of(new BigDecimal("14.99"), "EUR");

        assertThat(price.multiply(2).amount()).isEqualByComparingTo(new BigDecimal("29.98"));
        assertThat(price.add(Money.of(new BigDecimal("5.00"), "EUR")).amount())
                .isEqualByComparingTo(new BigDecimal("19.99"));
    }

    @Test
    void rejectsInvalidAmountsCurrenciesAndArithmetic() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-0.01"), "EUR"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("negative");
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, "EU1"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("ISO-4217");
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, "E\u00DCR"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("ISO-4217");
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, "EUR").multiply(0))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("greater than zero");
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, "EUR").add(Money.of(BigDecimal.ONE, "USD")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("same currency");
    }
}
