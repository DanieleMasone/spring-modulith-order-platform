package com.example.orderplatform.pricing.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogCurrencyPolicyTest {

    @Test
    void acceptsSingleCurrencyCatalogValues() {
        assertThatCode(() -> CatalogCurrencyPolicy.requireSingleCurrency(List.of(
                Money.of(new BigDecimal("14.99"), "EUR"),
                Money.of(new BigDecimal("19.99"), "EUR"))))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMixedCurrencyCatalogValues() {
        assertThatThrownBy(() -> CatalogCurrencyPolicy.requireSingleCurrency(List.of(
                Money.of(new BigDecimal("14.99"), "EUR"),
                Money.of(new BigDecimal("19.99"), "USD"))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("mix currencies");
    }
}
