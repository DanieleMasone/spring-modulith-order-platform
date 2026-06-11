package com.example.orderplatform.orders.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain event published after an order is persisted.
 *
 * <p>Payments use this event to prepare a pending payment record; notifications use it to record a
 * customer-facing notification intent.
 *
 * @param orderId submitted order identifier
 * @param customerId customer that placed the order
 * @param totalAmount accepted order total amount
 * @param currency accepted order total currency
 * @param occurredAt order creation timestamp
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        String currency,
        OffsetDateTime occurredAt) {
}
