package com.example.orderplatform.orders.infrastructure;

import com.example.orderplatform.orders.domain.OrderDraft;
import com.example.orderplatform.orders.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private UUID id;
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;
    private String currency;
    private OffsetDateTime createdAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineEntity> lines = new ArrayList<>();

    protected OrderEntity() {
    }

    private OrderEntity(UUID id, UUID customerId, OrderStatus status, BigDecimal totalAmount, String currency, OffsetDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public static OrderEntity from(UUID id, OrderDraft draft, OffsetDateTime createdAt) {
        OrderEntity order = new OrderEntity(
                id,
                draft.customerId(),
                OrderStatus.SUBMITTED,
                draft.total().amount(),
                draft.total().currency(),
                createdAt);

        draft.lines().forEach(line -> order.lines.add(OrderLineEntity.from(order, line)));
        return order;
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public BigDecimal totalAmount() {
        return totalAmount;
    }

    public String currency() {
        return currency;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public List<OrderLineEntity> lines() {
        return Collections.unmodifiableList(lines);
    }
}
