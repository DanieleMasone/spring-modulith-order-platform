package com.example.orderplatform.orders.infrastructure;

import com.example.orderplatform.orders.domain.OrderLineDraft;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_lines")
public class OrderLineEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private String productCode;
    private int quantity;
    private BigDecimal unitAmount;
    private String currency;

    protected OrderLineEntity() {
    }

    private OrderLineEntity(UUID id, OrderEntity order, String productCode, int quantity, BigDecimal unitAmount, String currency) {
        this.id = id;
        this.order = order;
        this.productCode = productCode;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
        this.currency = currency;
    }

    static OrderLineEntity from(OrderEntity order, OrderLineDraft line) {
        return new OrderLineEntity(
                UUID.randomUUID(),
                order,
                line.productCode(),
                line.quantity(),
                line.unitPrice().amount(),
                line.unitPrice().currency());
    }

    public String productCode() {
        return productCode;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal unitAmount() {
        return unitAmount;
    }

    public String currency() {
        return currency;
    }
}
