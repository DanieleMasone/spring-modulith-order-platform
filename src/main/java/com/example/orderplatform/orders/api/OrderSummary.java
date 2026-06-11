package com.example.orderplatform.orders.api;

import com.example.orderplatform.Money;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Immutable order view returned by the order module and REST layer.
 *
 * @param id order identifier
 * @param customerId customer that placed the order
 * @param status current order status
 * @param total order total
 * @param lines priced order lines
 * @param createdAt creation timestamp
 */
public record OrderSummary(
        UUID id,
        UUID customerId,
        String status,
        Money total,
        List<OrderLineSummary> lines,
        OffsetDateTime createdAt) {
}
