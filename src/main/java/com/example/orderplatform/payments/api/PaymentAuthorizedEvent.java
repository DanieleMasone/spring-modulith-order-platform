package com.example.orderplatform.payments.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain event published after a payment is authorized.
 *
 * <p>Notifications use this event to record the operational notification intent for a completed
 * authorization.
 *
 * @param paymentId authorized payment identifier
 * @param orderId order associated with the payment
 * @param amount authorized amount
 * @param currency authorized currency
 * @param occurredAt authorization timestamp
 */
public record PaymentAuthorizedEvent(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String currency,
        OffsetDateTime occurredAt) {
}
