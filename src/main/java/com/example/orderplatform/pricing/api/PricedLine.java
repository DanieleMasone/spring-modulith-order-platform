package com.example.orderplatform.pricing.api;

import com.example.orderplatform.Money;

/**
 * Price calculation for one requested product line.
 *
 * @param productCode catalog product code
 * @param quantity quoted quantity
 * @param unitPrice catalog unit price
 * @param lineTotal unit price multiplied by quantity
 */
public record PricedLine(String productCode, int quantity, Money unitPrice, Money lineTotal) {
}
