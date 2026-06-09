package com.example.orderplatform.pricing.api;

import com.example.orderplatform.Money;

public record PricedLine(String productCode, int quantity, Money unitPrice, Money lineTotal) {
}
