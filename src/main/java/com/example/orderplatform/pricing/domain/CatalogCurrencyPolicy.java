package com.example.orderplatform.pricing.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.util.List;

public final class CatalogCurrencyPolicy {

    private CatalogCurrencyPolicy() {
    }

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
