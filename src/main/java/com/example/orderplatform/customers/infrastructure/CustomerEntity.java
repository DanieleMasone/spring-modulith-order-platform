package com.example.orderplatform.customers.infrastructure;

import com.example.orderplatform.customers.domain.CustomerStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    private UUID id;
    private String email;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    private OffsetDateTime createdAt;

    protected CustomerEntity() {
    }

    public CustomerEntity(UUID id, String email, String fullName, CustomerStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String fullName() {
        return fullName;
    }

    public CustomerStatus status() {
        return status;
    }
}
