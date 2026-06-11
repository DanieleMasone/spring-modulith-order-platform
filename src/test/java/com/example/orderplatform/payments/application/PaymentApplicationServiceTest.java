package com.example.orderplatform.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderplatform.Money;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.payments.api.PaymentAuthorizedEvent;
import com.example.orderplatform.payments.api.PaymentSummary;
import com.example.orderplatform.payments.domain.Payment;
import com.example.orderplatform.payments.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

    @Mock
    PaymentStore payments;

    @Mock
    ApplicationEventPublisher events;

    @Test
    void preparesPendingPaymentForCommittedOrder() {
        UUID orderId = UUID.randomUUID();
        when(payments.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(payments.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new PaymentApplicationService(payments, events);
        service.preparePayment(new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                new BigDecimal("49.97"),
                "EUR",
                OffsetDateTime.now()));

        ArgumentCaptor<Payment> savedPayment = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(savedPayment.capture());
        assertThat(savedPayment.getValue().orderId()).isEqualTo(orderId);
        assertThat(savedPayment.getValue().status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void authorizesMatchingPendingPaymentAndPublishesEvent() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Money amount = Money.of(new BigDecimal("49.97"), "EUR");
        Payment pending = Payment.pending(paymentId, orderId, amount, OffsetDateTime.now());

        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(pending));
        when(payments.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new PaymentApplicationService(payments, events);
        PaymentSummary summary = service.authorize(orderId, amount);

        assertThat(summary.status()).isEqualTo("AUTHORIZED");

        ArgumentCaptor<Payment> savedPayment = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(savedPayment.capture());
        assertThat(savedPayment.getValue().status()).isEqualTo(PaymentStatus.AUTHORIZED);

        ArgumentCaptor<PaymentAuthorizedEvent> event = ArgumentCaptor.forClass(PaymentAuthorizedEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().paymentId()).isEqualTo(paymentId);
        assertThat(event.getValue().orderId()).isEqualTo(orderId);
    }
}
