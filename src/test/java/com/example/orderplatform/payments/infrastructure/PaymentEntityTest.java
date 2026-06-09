package com.example.orderplatform.payments.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import com.example.orderplatform.payments.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentEntityTest {

    @Test
    void authorizesPendingPayment() {
        var authorizedAt = OffsetDateTime.now();
        var payment = PaymentEntity.pending(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("49.97"), "EUR"),
                OffsetDateTime.now());

        payment.authorize(authorizedAt);

        assertThat(payment.status()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(payment.authorizedAt()).isEqualTo(authorizedAt);
    }

    @Test
    void rejectsAuthorizingPaymentTwice() {
        var payment = PaymentEntity.pending(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Money.of(new BigDecimal("49.97"), "EUR"),
                OffsetDateTime.now());
        payment.authorize(OffsetDateTime.now());

        assertThatThrownBy(() -> payment.authorize(OffsetDateTime.now()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("pending payments");
    }
}
