package com.example.orderplatform.payments.application;

import com.example.orderplatform.payments.domain.Payment;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for prepared and authorized payments.
 */
public interface PaymentStore {

    /**
     * Finds a payment by identifier.
     *
     * @param paymentId payment identifier
     * @return payment when present
     */
    Optional<Payment> findById(UUID paymentId);

    /**
     * Finds the payment prepared for an order.
     *
     * @param orderId order identifier
     * @return payment when present
     */
    Optional<Payment> findByOrderId(UUID orderId);

    /**
     * Persists the current payment state.
     *
     * @param payment payment state to save
     * @return saved payment
     */
    Payment save(Payment payment);
}
