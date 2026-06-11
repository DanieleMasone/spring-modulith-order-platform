package com.example.orderplatform.payments.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void authorizesPendingPayment() {
        var authorizedAt = OffsetDateTime.now();
        var payment = Payment.pending(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("49.97"), "EUR"),
                OffsetDateTime.now());

        Payment authorized = payment.authorize(authorizedAt);

        assertThat(authorized.status()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(authorized.authorizedAt()).isEqualTo(authorizedAt);
        assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void rejectsAuthorizingPaymentTwice() {
        var payment = Payment.pending(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("49.97"), "EUR"),
                OffsetDateTime.now())
                .authorize(OffsetDateTime.now());

        assertThatThrownBy(() -> payment.authorize(OffsetDateTime.now()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("pending payments");
    }
}
