package com.example.orderplatform.payments.api;

import com.example.orderplatform.Money;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentSummary(
        UUID id,
        UUID orderId,
        String status,
        Money amount,
        OffsetDateTime createdAt,
        OffsetDateTime authorizedAt) {
}
