package com.example.orderplatform.orders.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderplatform.Money;
import com.example.orderplatform.customers.api.CustomerDirectory;
import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.orders.api.OrderCommand;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.orders.api.OrderItemCommand;
import com.example.orderplatform.orders.api.OrderLineSummary;
import com.example.orderplatform.orders.api.OrderSummary;
import com.example.orderplatform.orders.domain.OrderDraft;
import com.example.orderplatform.pricing.api.PricedLine;
import com.example.orderplatform.pricing.api.PricingQuote;
import com.example.orderplatform.pricing.api.PricingRequest;
import com.example.orderplatform.pricing.api.PricingService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    CustomerDirectory customers;

    @Mock
    PricingService pricing;

    @Mock
    OrderStore orders;

    @Mock
    ApplicationEventPublisher events;

    @Test
    void placesOrderThroughModuleApisAndOrderStore() {
        UUID customerId = UUID.randomUUID();
        Money unitPrice = Money.of(new BigDecimal("14.99"), "EUR");
        Money total = Money.of(new BigDecimal("29.98"), "EUR");

        when(customers.getRequiredCustomer(customerId))
                .thenReturn(new CustomerSnapshot(customerId, "ada@example.com", "Ada Lovelace", "ACTIVE"));
        when(pricing.quoteFor(List.of(new PricingRequest("SKU-COFFEE-MUG", 2))))
                .thenReturn(new PricingQuote(List.of(new PricedLine("SKU-COFFEE-MUG", 2, unitPrice, total)), total));
        when(orders.save(any(UUID.class), any(OrderDraft.class), any(OffsetDateTime.class)))
                .thenAnswer(invocation -> new OrderSummary(
                        invocation.getArgument(0),
                        customerId,
                        "SUBMITTED",
                        total,
                        List.of(new OrderLineSummary("SKU-COFFEE-MUG", 2, unitPrice, total)),
                        invocation.getArgument(2)));

        var service = new OrderApplicationService(customers, pricing, orders, events);
        OrderSummary summary = service.placeOrder(new OrderCommand(
                customerId,
                List.of(new OrderItemCommand("SKU-COFFEE-MUG", 2))));

        assertThat(summary.total()).isEqualTo(total);

        ArgumentCaptor<OrderDraft> draft = ArgumentCaptor.forClass(OrderDraft.class);
        verify(orders).save(any(UUID.class), draft.capture(), any(OffsetDateTime.class));
        assertThat(draft.getValue().customerId()).isEqualTo(customerId);
        assertThat(draft.getValue().lines()).hasSize(1);

        ArgumentCaptor<OrderCreatedEvent> event = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().orderId()).isEqualTo(summary.id());
        assertThat(event.getValue().totalAmount()).isEqualByComparingTo(new BigDecimal("29.98"));
    }
}
