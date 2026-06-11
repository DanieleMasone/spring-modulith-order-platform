package com.example.orderplatform.orders.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;

/**
 * Validated priced line used to create a persisted order.
 *
 * @param productCode catalog product code
 * @param quantity positive ordered quantity
 * @param unitPrice accepted unit price
 */
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

    /**
     * Calculates the line total from unit price and quantity.
     *
     * @return line total
     */
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
