package com.example.orderplatform.payments.infrastructure;

import com.example.orderplatform.Money;
import com.example.orderplatform.payments.application.PaymentStore;
import com.example.orderplatform.payments.domain.Payment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class JpaPaymentStore implements PaymentStore {

    private final PaymentRepository payments;

    JpaPaymentStore(PaymentRepository payments) {
        this.payments = payments;
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return payments.findById(paymentId).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return payments.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = payments.findById(payment.id())
                .map(existing -> {
                    existing.updateFrom(payment);
                    return existing;
                })
                .orElseGet(() -> PaymentEntity.from(payment));
        return toDomain(payments.save(entity));
    }

    private Payment toDomain(PaymentEntity payment) {
        return new Payment(
                payment.id(),
                payment.orderId(),
                payment.status(),
                Money.of(payment.amount(), payment.currency()),
                payment.createdAt(),
                payment.authorizedAt());
    }
}
