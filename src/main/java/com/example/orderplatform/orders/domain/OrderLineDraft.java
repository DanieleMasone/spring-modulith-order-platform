package com.example.orderplatform.orders.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;

public record OrderLineDraft(String productCode, int quantity, Money unitPrice) {

    public OrderLineDraft {
        if (productCode == null || productCode.isBlank()) {
            throw new BusinessRuleViolationException("Product code is required.");
        }
        if (quantity < 1) {
            throw new BusinessRuleViolationException("Quantity must be greater than zero.");
        }
        if (unitPrice == null) {
            throw new BusinessRuleViolationException("Unit price is required.");
        }
    }

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
