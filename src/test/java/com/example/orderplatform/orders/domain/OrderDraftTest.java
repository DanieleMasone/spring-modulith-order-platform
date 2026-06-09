package com.example.orderplatform.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderDraftTest {

    @Test
    void calculatesOrderTotalFromLines() {
        var draft = new OrderDraft(UUID.randomUUID(), List.of(
                new OrderLineDraft("SKU-COFFEE-MUG", 2, Money.of(new BigDecimal("14.99"), "EUR")),
                new OrderLineDraft("SKU-NOTEBOOK", 1, Money.of(new BigDecimal("19.99"), "EUR"))));

        assertThat(draft.total().amount()).isEqualByComparingTo(new BigDecimal("49.97"));
    }

    @Test
    void rejectsEmptyOrders() {
        assertThatThrownBy(() -> new OrderDraft(UUID.randomUUID(), List.of()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("At least one order line");
    }

    @Test
    void rejectsInvalidOrderLines() {
        assertThatThrownBy(() -> new OrderLineDraft("", 1, Money.of(new BigDecimal("14.99"), "EUR")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Product code");

        assertThatThrownBy(() -> new OrderLineDraft("SKU-COFFEE-MUG", 0, Money.of(new BigDecimal("14.99"), "EUR")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Quantity");

        assertThatThrownBy(() -> new OrderLineDraft("SKU-COFFEE-MUG", 1, null))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Unit price");
    }

    @Test
    void rejectsMixedCurrencies() {
        assertThatThrownBy(() -> new OrderDraft(UUID.randomUUID(), List.of(
                new OrderLineDraft("SKU-COFFEE-MUG", 1, Money.of(new BigDecimal("14.99"), "EUR")),
                new OrderLineDraft("SKU-NOTEBOOK", 1, Money.of(new BigDecimal("19.99"), "USD")))))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("same currency");
    }
}
