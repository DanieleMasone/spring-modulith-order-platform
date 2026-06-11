package com.example.orderplatform.orders.api;

import java.util.List;
import java.util.UUID;

/**
 * Order placement command accepted by the order module.
 *
 * @param customerId customer placing the order
 * @param items requested product lines
 */
public record OrderCommand(UUID customerId, List<OrderItemCommand> items) {
}
