package com.example.orderplatform.orders.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.util.List;
import java.util.UUID;

/**
 * Validated order draft created after pricing has accepted each requested line.
 *
 * @param customerId customer placing the order
 * @param lines priced order lines
 */
public record OrderDraft(UUID customerId, List<OrderLineDraft> lines) {

    public OrderDraft {
        if (customerId == null) {
            throw new BusinessRuleViolationException("Customer is required to create an order.");
        }
        if (lines == null || lines.isEmpty()) {
            throw new BusinessRuleViolationException("At least one order line is required.");
        }
        lines = List.copyOf(lines);
        calculateTotal(lines);
    }

    /**
     * Calculates the order total from all priced lines.
     *
     * @return total monetary value
     */
    public Money total() {
        return calculateTotal(lines);
    }

    private static Money calculateTotal(List<OrderLineDraft> lines) {
        return lines.stream()
                .map(OrderLineDraft::lineTotal)
                .reduce((left, right) -> left.add(right))
                .orElseThrow();
    }
}
