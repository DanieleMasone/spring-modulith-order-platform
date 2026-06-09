package com.example.orderplatform.orders.api;

import com.example.orderplatform.Money;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderSummary(
        UUID id,
        UUID customerId,
        String status,
        Money total,
        List<OrderLineSummary> lines,
        OffsetDateTime createdAt) {
}
