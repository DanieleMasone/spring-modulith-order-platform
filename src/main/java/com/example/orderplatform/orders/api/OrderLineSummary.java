package com.example.orderplatform.orders.api;

import com.example.orderplatform.Money;

/**
 * Immutable view of one persisted order line.
 *
 * @param productCode catalog product code
 * @param quantity ordered quantity
 * @param unitPrice accepted unit price
 * @param lineTotal accepted line total
 */
public record OrderLineSummary(String productCode, int quantity, Money unitPrice, Money lineTotal) {
}
