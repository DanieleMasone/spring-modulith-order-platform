package com.example.orderplatform.pricing.application;

import com.example.orderplatform.Money;

/**
 * Active catalog price exposed to pricing use cases without leaking persistence details.
 *
 * @param productCode catalog product code
 * @param unitPrice active unit price
 */
public record CatalogPrice(String productCode, Money unitPrice) {
}
