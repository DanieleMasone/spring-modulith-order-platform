package com.example.orderplatform.payments.api;

import com.example.orderplatform.Money;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable payment view exposed by the payment module API.
 *
 * @param id payment identifier
 * @param orderId order associated with the payment
 * @param status current payment status
 * @param amount expected or authorized payment amount
 * @param createdAt creation timestamp
 * @param authorizedAt authorization timestamp, or {@code null} while pending
 */
public record PaymentSummary(
        UUID id,
        UUID orderId,
        String status,
        Money amount,
        OffsetDateTime createdAt,
        OffsetDateTime authorizedAt) {
}
