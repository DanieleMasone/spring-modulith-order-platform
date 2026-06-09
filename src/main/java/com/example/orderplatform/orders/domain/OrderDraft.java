package com.example.orderplatform.orders.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.util.List;
import java.util.UUID;

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
