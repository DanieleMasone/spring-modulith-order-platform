package com.example.orderplatform.pricing.api;

import com.example.orderplatform.Money;
import java.util.List;

public record PricingQuote(List<PricedLine> lines, Money total) {
}
