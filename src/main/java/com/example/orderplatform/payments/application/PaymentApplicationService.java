package com.example.orderplatform.payments.application;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.payments.api.PaymentAuthorizedEvent;
import com.example.orderplatform.payments.api.PaymentManagement;
import com.example.orderplatform.payments.api.PaymentSummary;
import com.example.orderplatform.payments.infrastructure.PaymentEntity;
import com.example.orderplatform.payments.infrastructure.PaymentRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Transactional
public class PaymentApplicationService implements PaymentManagement {

    private final PaymentRepository payments;
    private final ApplicationEventPublisher events;

    public PaymentApplicationService(PaymentRepository payments, ApplicationEventPublisher events) {
        this.payments = payments;
        this.events = events;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void preparePayment(OrderCreatedEvent event) {
        payments.findByOrderId(event.orderId()).orElseGet(() -> payments.save(PaymentEntity.pending(
                UUID.randomUUID(),
                event.orderId(),
                Money.of(event.totalAmount(), event.currency()),
                OffsetDateTime.now())));
    }

    @Override
    public PaymentSummary authorize(UUID orderId, Money amount) {
        PaymentEntity payment = payments.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order " + orderId + " was not found."));

        if (!payment.amount().equals(amount.amount()) || !payment.currency().equals(amount.currency())) {
            throw new BusinessRuleViolationException("Payment amount must match the submitted order total.");
        }

        payment.authorize(OffsetDateTime.now());
        PaymentSummary summary = toSummary(payment);
        events.publishEvent(new PaymentAuthorizedEvent(
                summary.id(),
                summary.orderId(),
                summary.amount().amount(),
                summary.amount().currency(),
                summary.authorizedAt()));
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummary getPayment(UUID paymentId) {
        return payments.findById(paymentId)
                .map(this::toSummary)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " was not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentSummary> findByOrderId(UUID orderId) {
        return payments.findByOrderId(orderId).map(this::toSummary);
    }

    private PaymentSummary toSummary(PaymentEntity payment) {
        return new PaymentSummary(
                payment.id(),
                payment.orderId(),
                payment.status().name(),
                Money.of(payment.amount(), payment.currency()),
                payment.createdAt(),
                payment.authorizedAt());
    }
}
