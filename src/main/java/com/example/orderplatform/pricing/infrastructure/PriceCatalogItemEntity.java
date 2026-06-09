package com.example.orderplatform.pricing.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "price_catalog")
public class PriceCatalogItemEntity {

    @Id
    private String productCode;
    private String description;
    private BigDecimal unitAmount;
    private String currency;
    private boolean active;
    private OffsetDateTime updatedAt;

    protected PriceCatalogItemEntity() {
    }

    public PriceCatalogItemEntity(String productCode, String description, BigDecimal unitAmount, String currency, boolean active, OffsetDateTime updatedAt) {
        this.productCode = productCode;
        this.description = description;
        this.unitAmount = unitAmount;
        this.currency = currency;
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public String productCode() {
        return productCode;
    }

    public BigDecimal unitAmount() {
        return unitAmount;
    }

    public String currency() {
        return currency;
    }

    public boolean active() {
        return active;
    }
}
