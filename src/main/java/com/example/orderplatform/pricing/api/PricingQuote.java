package com.example.orderplatform.pricing.api;

import com.example.orderplatform.Money;
import java.util.List;

/**
 * Completed quote returned by the pricing module.
 *
 * @param lines priced request lines
 * @param total aggregate quote total
 */
public record PricingQuote(List<PricedLine> lines, Money total) {
}
