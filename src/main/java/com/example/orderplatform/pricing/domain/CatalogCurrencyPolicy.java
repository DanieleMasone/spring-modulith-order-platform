package com.example.orderplatform.pricing.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.util.List;

/**
 * Domain policy that keeps catalog quote totals in a single currency.
 */
public final class CatalogCurrencyPolicy {

    private CatalogCurrencyPolicy() {
    }

    /**
     * Verifies that all values in a quote use the same currency.
     *
     * @param values monetary values to inspect
     * @throws BusinessRuleViolationException when more than one currency is present
     */
    public static void requireSingleCurrency(List<Money> values) {
        if (values.isEmpty()) {
            return;
        }
        String currency = values.getFirst().currency();
        boolean mixedCurrencies = values.stream().anyMatch(value -> !currency.equals(value.currency()));
        if (mixedCurrencies) {
            throw new BusinessRuleViolationException("A quote cannot mix currencies.");
        }
    }
}
