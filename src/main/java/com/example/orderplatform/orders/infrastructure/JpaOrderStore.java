package com.example.orderplatform.orders.infrastructure;

import com.example.orderplatform.Money;
import com.example.orderplatform.orders.api.OrderLineSummary;
import com.example.orderplatform.orders.api.OrderSummary;
import com.example.orderplatform.orders.application.OrderStore;
import com.example.orderplatform.orders.domain.OrderDraft;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class JpaOrderStore implements OrderStore {

    private final OrderRepository orders;

    JpaOrderStore(OrderRepository orders) {
        this.orders = orders;
    }

    @Override
    public OrderSummary save(UUID id, OrderDraft draft, OffsetDateTime createdAt) {
        return toSummary(orders.save(OrderEntity.from(id, draft, createdAt)));
    }

    @Override
    public Optional<OrderSummary> findById(UUID orderId) {
        return orders.findById(orderId).map(this::toSummary);
    }

    private OrderSummary toSummary(OrderEntity order) {
        List<OrderLineSummary> lines = order.lines().stream()
                .map(this::toLineSummary)
                .toList();
        return new OrderSummary(
                order.id(),
                order.customerId(),
                order.status().name(),
                Money.of(order.totalAmount(), order.currency()),
                lines,
                order.createdAt());
    }

    private OrderLineSummary toLineSummary(OrderLineEntity line) {
        Money unitPrice = Money.of(line.unitAmount(), line.currency());
        return new OrderLineSummary(line.productCode(), line.quantity(), unitPrice, unitPrice.multiply(line.quantity()));
    }
}
