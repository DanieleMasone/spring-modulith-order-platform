package com.example.orderplatform.payments.api;

import com.example.orderplatform.Money;
import java.util.Optional;
import java.util.UUID;

public interface PaymentManagement {

    PaymentSummary authorize(UUID orderId, Money amount);

    PaymentSummary getPayment(UUID paymentId);

    Optional<PaymentSummary> findByOrderId(UUID orderId);
}
