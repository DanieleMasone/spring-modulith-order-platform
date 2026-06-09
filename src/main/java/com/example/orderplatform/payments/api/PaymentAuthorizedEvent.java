package com.example.orderplatform.payments.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentAuthorizedEvent(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String currency,
        OffsetDateTime occurredAt) {
}
