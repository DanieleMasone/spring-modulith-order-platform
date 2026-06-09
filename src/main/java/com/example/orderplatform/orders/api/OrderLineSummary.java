package com.example.orderplatform.orders.api;

import com.example.orderplatform.Money;

public record OrderLineSummary(String productCode, int quantity, Money unitPrice, Money lineTotal) {
}
