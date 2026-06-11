package com.example.orderplatform.payments.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Framework-independent payment state used to enforce authorization transitions.
 *
 * @param id payment identifier
 * @param orderId related order identifier
 * @param status current payment status
 * @param amount expected amount
 * @param createdAt creation timestamp
 * @param authorizedAt authorization timestamp when authorized
 */
public record Payment(
        UUID id,
        UUID orderId,
        PaymentStatus status,
        Money amount,
        OffsetDateTime createdAt,
        OffsetDateTime authorizedAt) {

    public Payment {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(orderId, "orderId is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        if (status == PaymentStatus.AUTHORIZED && authorizedAt == null) {
            throw new BusinessRuleViolationException("Authorized payments must include an authorization timestamp.");
        }
    }

    /**
     * Creates a pending payment for a submitted order.
     *
     * @param id generated payment identifier
     * @param orderId related order identifier
     * @param amount expected payment amount
     * @param createdAt creation timestamp
     * @return pending payment
     */
    public static Payment pending(UUID id, UUID orderId, Money amount, OffsetDateTime createdAt) {
        return new Payment(id, orderId, PaymentStatus.PENDING, amount, createdAt, null);
    }

    /**
     * Authorizes this payment when it is still pending.
     *
     * @param authorizedAt authorization timestamp
     * @return authorized payment state
     * @throws BusinessRuleViolationException when the payment is not pending
     */
    public Payment authorize(OffsetDateTime authorizedAt) {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending payments can be authorized.");
        }
        return new Payment(id, orderId, PaymentStatus.AUTHORIZED, amount, createdAt, authorizedAt);
    }
}
