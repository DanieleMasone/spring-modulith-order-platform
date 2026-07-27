package com.example.orderplatform.payments.api;

import com.example.orderplatform.Money;
import java.util.UUID;

/**
 * Module API for authorizing and retrieving payments associated with orders.
 */
public interface PaymentManagement {

    /**
     * Authorizes the pending payment for an order when the submitted amount matches the order total.
     *
     * @param orderId order identifier
     * @param amount submitted payment amount
     * @return authorized payment summary
     * @throws com.example.orderplatform.ResourceNotFoundException when no payment exists for the order
     * @throws com.example.orderplatform.BusinessRuleViolationException when the submitted amount does not match
     */
    PaymentSummary authorize(UUID orderId, Money amount);

    /**
     * Retrieves a payment by identifier.
     *
     * @param paymentId payment identifier
     * @return payment summary
     * @throws com.example.orderplatform.ResourceNotFoundException when the payment does not exist
     */
    PaymentSummary getPayment(UUID paymentId);

}
