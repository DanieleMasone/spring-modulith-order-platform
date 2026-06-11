package com.example.orderplatform.orders.api;

/**
 * Requested product line in an order command.
 *
 * @param productCode catalog product code
 * @param quantity positive requested quantity
 */
public record OrderItemCommand(String productCode, int quantity) {
}
