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
