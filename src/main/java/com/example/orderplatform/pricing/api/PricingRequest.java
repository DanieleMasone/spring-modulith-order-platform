package com.example.orderplatform.pricing.api;

/**
 * Product quantity requested for pricing.
 *
 * @param productCode catalog product code
 * @param quantity positive requested quantity
 */
public record PricingRequest(String productCode, int quantity) {
}
