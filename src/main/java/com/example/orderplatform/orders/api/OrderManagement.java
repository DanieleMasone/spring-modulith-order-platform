package com.example.orderplatform.orders.api;

import java.util.UUID;

/**
 * Module API for placing and retrieving orders.
 */
public interface OrderManagement {

    /**
     * Places an order after customer validation and pricing.
     *
     * @param command order placement command
     * @return submitted order summary
     * @throws com.example.orderplatform.ResourceNotFoundException when the customer or a product cannot be found
     * @throws com.example.orderplatform.BusinessRuleViolationException when the order command is invalid
     */
    OrderSummary placeOrder(OrderCommand command);

    /**
     * Retrieves an order by identifier.
     *
     * @param orderId order identifier
     * @return submitted order summary
     * @throws com.example.orderplatform.ResourceNotFoundException when the order does not exist
     */
    OrderSummary getOrder(UUID orderId);
}
