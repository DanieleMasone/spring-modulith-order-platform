package com.example.orderplatform.payments.infrastructure;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import com.example.orderplatform.payments.domain.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    private UUID id;
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private BigDecimal amount;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime authorizedAt;

    @Version
    private long version;

    protected PaymentEntity() {
    }

    private PaymentEntity(UUID id, UUID orderId, PaymentStatus status, BigDecimal amount, String currency, OffsetDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public static PaymentEntity pending(UUID id, UUID orderId, Money amount, OffsetDateTime createdAt) {
        return new PaymentEntity(id, orderId, PaymentStatus.PENDING, amount.amount(), amount.currency(), createdAt);
    }

    public void authorize(OffsetDateTime authorizedAt) {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending payments can be authorized.");
        }
        this.status = PaymentStatus.AUTHORIZED;
        this.authorizedAt = authorizedAt;
    }

    public UUID id() {
        return id;
    }

    public UUID orderId() {
        return orderId;
    }

    public PaymentStatus status() {
        return status;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime authorizedAt() {
        return authorizedAt;
    }
}
